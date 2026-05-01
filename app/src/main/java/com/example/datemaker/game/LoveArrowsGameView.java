package com.example.datemaker.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.datemaker.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class LoveArrowsGameView extends SurfaceView implements SurfaceHolder.Callback {

    public interface GameListener {
        void onScoreChanged(int newScore);
        void onArrowUsed(int remaining);
    }

    private GameThread thread;
    private final List<Target> targets = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();

    private int score = 0;
    private int arrowsLeft = 10;
    private GameListener listener;
    private final Random random = new Random();
    private long lastSpawnTime = 0;

    // Device orientation (relative to starting direction)
    private float deviceAzimuth = 0f; // radians
    private float devicePitch   = 0f; // radians

    // Field of view (radians) – fairly generous for an arcade feel
    static final float HORIZONTAL_FOV = (float) Math.toRadians(100);
    static final float VERTICAL_FOV   = (float) Math.toRadians(70);

    // Sprites
    private Bitmap bmpStaticHeart;
    private Bitmap bmpMovingHeart;
    private Bitmap bmpPenguin;

    // Input → game-thread bridge (avoid concurrent list modification)
    private int pendingShots = 0;

    public LoveArrowsGameView(Context context) {
        super(context);
        init();
    }

    public LoveArrowsGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoveArrowsGameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT); // draw over camera preview

        // Load sprites
        bmpStaticHeart = BitmapFactory.decodeResource(getResources(), R.drawable.heart_static);
        bmpMovingHeart = BitmapFactory.decodeResource(getResources(), R.drawable.heart_moving);
        bmpPenguin     = BitmapFactory.decodeResource(getResources(), R.drawable.penguin_linux);
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    // Called from activity when orientation changes
    public void setDeviceOrientation(float azimuth, float pitch) {
        this.deviceAzimuth = azimuth;
        this.devicePitch   = pitch;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new GameThread(getHolder());
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (thread != null) {
            thread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException ignored) {}
            }
            thread = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && arrowsLeft > 0) {
            // Just signal a shot; the game thread will actually spawn the projectile
            pendingShots++;
            arrowsLeft--;
            if (listener != null) listener.onArrowUsed(arrowsLeft);
        }
        return true;
    }

    private class GameThread extends Thread {
        private final SurfaceHolder holder;
        private volatile boolean running;

        GameThread(SurfaceHolder holder) {
            this.holder = holder;
        }

        void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            while (running) {
                long now = System.currentTimeMillis();
                if (now - lastSpawnTime > 1000) {
                    spawnTarget();
                    lastSpawnTime = now;
                }

                update();
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) drawGame(canvas);
                } finally {
                    if (canvas != null) holder.unlockCanvasAndPost(canvas);
                }

                try {
                    sleep(16); // ~60 FPS
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void spawnTarget() {
        int type = random.nextInt(3); // 0 static heart, 1 moving heart, 2 penguin
        targets.add(new Target(type, random));
    }

    private void update() {
        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;

        // Handle new shots (only game thread touches projectiles list)
        while (pendingShots > 0) {
            projectiles.add(new Projectile(cx, cy));
            pendingShots--;
        }

        // Move projectiles
        for (Projectile p : projectiles) {
            p.update();
        }

        // Update targets and map to screen
        for (Target t : targets) {
            t.update();
            t.updateScreenPosition(deviceAzimuth, devicePitch, w, h);
        }

        // Collision detection around crosshair
        float hitRadius = 120f;

        Iterator<Projectile> pIt = projectiles.iterator();
        while (pIt.hasNext()) {
            Projectile p = pIt.next();
            Iterator<Target> tIt = targets.iterator();
            while (tIt.hasNext()) {
                Target t = tIt.next();
                if (!t.visibleOnScreen) continue;

                float dx = t.screenX - cx;
                float dy = t.screenY - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist <= hitRadius) {
                    score += t.points;
                    if (listener != null) listener.onScoreChanged(score);
                    tIt.remove();
                    pIt.remove();
                    break;
                }
            }
        }

        // Remove off-screen projectiles
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (p.y < 0) {
                it.remove();
            }
        }
    }

    private void drawGame(Canvas canvas) {
        // Clear previous frame
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;

        // Draw targets
        for (Target t : targets) {
            if (!t.visibleOnScreen) continue;

            Bitmap sprite;
            if (t.type == 2) {
                sprite = bmpPenguin;
            } else if (t.type == 1) {
                sprite = bmpMovingHeart;
            } else {
                sprite = bmpStaticHeart;
            }

            if (sprite != null && !sprite.isRecycled()) {
                float halfSize = t.size / 2f;
                float left   = t.screenX - halfSize;
                float top    = t.screenY - halfSize;
                float right  = t.screenX + halfSize;
                float bottom = t.screenY + halfSize;

                canvas.drawBitmap(
                        sprite,
                        null,
                        new android.graphics.RectF(left, top, right, bottom),
                        null
                );
            }
        }

        // Draw projectiles as arrows
        paint.setColor(Color.WHITE);
        for (Projectile p : projectiles) {
            drawArrow(canvas, paint, p);
        }

        // Draw crosshair (center)
        paint.setColor(Color.RED);
        paint.setStrokeWidth(4f);
        canvas.drawLine(cx - 40, cy, cx + 40, cy, paint); // horizontal
        canvas.drawLine(cx, cy - 40, cx, cy + 40, paint); // vertical
    }

    private void drawArrow(Canvas canvas, Paint paint, Projectile p) {
        float tipX = p.x;
        float tipY = p.y;

        float shaftLength = 60f;
        float tailY = tipY + shaftLength;

        // Shaft
        paint.setStrokeWidth(6f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(tipX, tipY, tipX, tailY, paint);

        // Arrow head (triangle)
        Paint.Style oldStyle = paint.getStyle();
        paint.setStyle(Paint.Style.FILL);

        Path head = new Path();
        head.moveTo(tipX, tipY);
        head.lineTo(tipX - 15, tipY + 25);
        head.lineTo(tipX + 15, tipY + 25);
        head.close();
        canvas.drawPath(head, paint);

        // Fletching near tail
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(tipX, tailY, tipX - 12, tailY + 15, paint);
        canvas.drawLine(tipX, tailY, tipX + 12, tailY + 15, paint);

        paint.setStyle(oldStyle);
    }

    // World-anchored target (bearing/elevation) mapped into screen
    private static class Target {
        int type; // 0 static heart, 1 moving heart, 2 penguin
        int points;

        float bearing;        // radians around user (left/right)
        float elevationAngle; // radians up/down
        float speed;          // radians/sec for moving types
        int size = 220;       // px

        float screenX, screenY;
        boolean visibleOnScreen = false;

        Target(int type, Random r) {
            this.type = type;

            float spawnCone = (float) Math.toRadians(60);
            this.bearing = (r.nextFloat() - 0.5f) * spawnCone;
            this.elevationAngle = (r.nextFloat() - 0.5f) * VERTICAL_FOV;

            if (type == 0) {
                points = 10;
                speed = 0f;
            } else if (type == 1) {
                points = 20;
                speed = (float) Math.toRadians(10);
            } else {
                points = 50;
                speed = (float) Math.toRadians(30);
            }
        }

        void update() {
            if (speed != 0f) {
                bearing += speed / 60f;
                if (bearing > Math.PI) bearing -= 2 * Math.PI;
                if (bearing < -Math.PI) bearing += 2 * Math.PI;
            }
        }

        void updateScreenPosition(float deviceAzimuth, float devicePitch,
                                  int screenW, int screenH) {

            // Relative angles: target - camera
            float relHoriz = normalizeAngle(bearing - deviceAzimuth);
            float relVert  = elevationAngle - devicePitch;

            // If outside FOV, don't render
            if (Math.abs(relHoriz) > HORIZONTAL_FOV / 2f ||
                    Math.abs(relVert)  > VERTICAL_FOV   / 2f) {
                visibleOnScreen = false;
                return;
            }

            // Map to [0,1] screen fractions
            float xRatio = 0.5f + (relHoriz / HORIZONTAL_FOV);
            float yRatio = 0.5f + (relVert  / VERTICAL_FOV);

            xRatio = Math.max(0f, Math.min(1f, xRatio));
            yRatio = Math.max(0f, Math.min(1f, yRatio));

            screenX = xRatio * screenW;
            screenY = yRatio * screenH;
            visibleOnScreen = true;
        }

        private static float normalizeAngle(float a) {
            while (a > Math.PI) a -= 2 * Math.PI;
            while (a < -Math.PI) a += 2 * Math.PI;
            return a;
        }
    }

    private static class Projectile {
        float x, y;
        float speed = 30f;

        Projectile(float startX, float startY) {
            this.x = startX;
            this.y = startY;
        }

        void update() {
            y -= speed;
        }
    }
}