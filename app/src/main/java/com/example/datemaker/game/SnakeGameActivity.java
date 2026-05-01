package com.example.datemaker.game;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.datemaker.R;
import com.example.datemaker.activity.GameCenterActivity;
import com.example.datemaker.databinding.ActivitySnakeGameBinding;

public class SnakeGameActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private ActivitySnakeGameBinding binding;

    private GameThread gameThread;
    private MediaPlayer btnClick;

    private long gameStartTime;
    private boolean isGameOver = false;

    private long currentScore = 0L;
    private long sessionCoins = 0L;
    private long sessionBestScore = 0L;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isGameOver) {
                updateHud();
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySnakeGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppConstants.initialize(this);

        AppConstants.getGameEngine().setGameOverListener(() ->
                runOnUiThread(this::showGameOverOverlay));

        binding.svSnakeGame.getHolder().addCallback(this);
        binding.svSnakeGame.setFocusable(true);

        btnClick = MediaPlayer.create(this, R.raw.button_click_snake);

        View.OnClickListener directionListener = this::changeSnakeDirection;
        binding.snakeUpButton.setOnClickListener(directionListener);
        binding.snakeRightButton.setOnClickListener(directionListener);
        binding.snakeLeftButton.setOnClickListener(directionListener);
        binding.snakeDownButton.setOnClickListener(directionListener);

        binding.btnRetrySnake.setOnClickListener(v -> onRetry());
        binding.btnContinueSnake.setOnClickListener(v -> onContinueToGameCenter());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottom = Math.max(systemBars.bottom, ime.bottom);

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    bottom
            );
            return insets;
        });
    }

    // SurfaceHolder.Callback ----------------------------------------

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        AppConstants.surfaceViewWidth = width;
        AppConstants.surfaceViewHeight = height;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        gameThread = new GameThread(holder);
        gameThread.setRunning(true);
        gameThread.start();

        startGameTimer();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (gameThread != null) {
            gameThread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try {
                    gameThread.join();
                    retry = false;
                } catch (InterruptedException ignored) {}
            }
        }
        timerHandler.removeCallbacks(timerRunnable);
    }

    // Game timer & HUD ----------------------------------------------

    private void startGameTimer() {
        gameStartTime = System.currentTimeMillis();
        isGameOver = false;
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);
    }

    private void updateHud() {
        long elapsedMillis = System.currentTimeMillis() - gameStartTime;
        long totalSeconds = elapsedMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        GameEngine engine = AppConstants.getGameEngine();

        long score = totalSeconds * 10L + engine.getFoodEaten() * 50L;
        currentScore = score;

        String timeText = String.format("TIME %02d:%02d", minutes, seconds);
        binding.tvTime.setText(timeText);
        binding.tvScore.setText("SCORE " + String.format("%04d", score));
    }

    // Game over overlay ---------------------------------------------

    private void showGameOverOverlay() {
        isGameOver = true;

        long coinsThisRun = currentScore / 10L;
        sessionCoins += coinsThisRun;

        if (currentScore > sessionBestScore) {
            sessionBestScore = currentScore;
        }

        binding.gameOverOverlay.setVisibility(View.VISIBLE);
    }

    private void onRetry() {
        binding.gameOverOverlay.setVisibility(View.GONE);
        AppConstants.getGameEngine().resetGame();
        startGameTimer();
    }

    private void onContinueToGameCenter() {
        Intent intent = new Intent(this, SnakeResultActivity.class);
        intent.putExtra("coinsEarned", sessionCoins);
        intent.putExtra("userBestScore", sessionBestScore);
        startActivity(intent);
        finish();
    }

    // Direction changes ---------------------------------------------

    private void changeSnakeDirection(View v) {
        if (btnClick != null) {
            btnClick.start();
        }

        String movement = String.valueOf(v.getTag());
        GameEngine engine = AppConstants.getGameEngine();

        switch (movement) {
            case "top":
                if (!"down".equals(engine.movingPosition)) {
                    engine.movingPosition = "top";
                }
                break;
            case "down":
                if (!"top".equals(engine.movingPosition)) {
                    engine.movingPosition = "down";
                }
                break;
            case "left":
                if (!"right".equals(engine.movingPosition)) {
                    engine.movingPosition = "left";
                }
                break;
            case "right":
                if (!"left".equals(engine.movingPosition)) {
                    engine.movingPosition = "right";
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}