package com.example.datemaker.game;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread{

    private final SurfaceHolder surfaceHolder;
    private boolean isRunning = false;

    private static final int FPS = 5;
    private static final long FRAME_TIME = 1000L / FPS;

    public GameThread(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;

        while (isRunning) {
            startTime = System.currentTimeMillis();

            Canvas canvas = null;

            try {
                canvas = surfaceHolder.lockCanvas();

                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        AppConstants.getGameEngine().update();
                        AppConstants.getGameEngine().draw(canvas);
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            timeMillis = System.currentTimeMillis() - startTime;
            waitTime = FRAME_TIME - timeMillis;

            if (waitTime > 0) {
                try {
                    sleep(waitTime);
                } catch (InterruptedException ignored) {

                }
            }
        }
    }
}
