package com.example.datemaker.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {

    // ========================
    // Game Over callback hook
    // ========================
    public interface GameOverListener {
        void onGameOver();
    }

    private GameOverListener gameOverListener;

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    // ========================
    // Direction (shared state)
    // ========================
    public String movingPosition = "right";

    // ========================
    // Grid
    // ========================
    private final int numColumns = 20;
    private final int numRows = 20;
    private int cellSize;
    private boolean sizeInitialized = false;

    // ========================
    // Snake + Food
    // ========================
    private final List<Point> snake = new ArrayList<>();
    private Point food;

    private final Random random = new Random();

    // ========================
    // Score data
    // ========================
    private int foodEaten = 0;
    private boolean isGameOver = false;

    // ========================
    // Paints
    // ========================
    private final Paint snakePaint = new Paint();
    private final Paint foodPaint = new Paint();
    private final Paint gridPaint = new Paint();

    public GameEngine() {
        snakePaint.setColor(Color.GREEN);
        foodPaint.setColor(Color.RED);

        gridPaint.setColor(Color.DKGRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
    }

    // ========================
    // Size init
    // ========================
    private void ensureSize() {
        if (sizeInitialized) return;

        int w = AppConstants.surfaceViewWidth;
        int h = AppConstants.surfaceViewHeight;

        if (w == 0 || h == 0) return;

        cellSize = Math.min(w / numColumns, h / numRows);
        sizeInitialized = true;
        resetGame();
    }

    // ========================
    // PUBLIC RESET METHOD
    // ========================
    public void resetGame() {

        snake.clear();

        int centerX = numColumns / 2;
        int centerY = numRows / 2;

        // Start length = 3
        snake.add(new Point(centerX, centerY));
        snake.add(new Point(centerX - 1, centerY));
        snake.add(new Point(centerX - 2, centerY));

        movingPosition = "right";
        foodEaten = 0;
        isGameOver = false;

        spawnFood();
    }

    // ========================
    // Food
    // ========================
    private void spawnFood() {
        Point newFood;

        do {
            newFood = new Point(
                    random.nextInt(numColumns),
                    random.nextInt(numRows)
            );
        } while (snake.contains(newFood));

        food = newFood;
    }

    // ========================
    // GETTERS USED BY HUD
    // ========================
    public int getFoodEaten() {
        return foodEaten;
    }

    public int getLength() {
        return snake.size();
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    // ========================
    // UPDATE LOOP
    // ========================
    public void update() {
        ensureSize();

        if (!sizeInitialized) return;
        if (isGameOver) return;

        Point head = snake.get(0);

        int newX = head.x;
        int newY = head.y;

        switch (movingPosition) {
            case "top":
                newY--;
                break;
            case "down":
                newY++;
                break;
            case "left":
                newX--;
                break;
            case "right":
                newX++;
                break;
        }

        Point newHead = new Point(newX, newY);

        boolean hitWall =
                newX < 0 || newX >= numColumns ||
                        newY < 0 || newY >= numRows;

        boolean hitSelf = snake.contains(newHead);

        if (hitWall || hitSelf) {
            triggerGameOver();
            return;
        }

        snake.add(0, newHead);

        if (newHead.equals(food)) {
            foodEaten++;
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void triggerGameOver() {
        isGameOver = true;

        if (gameOverListener != null) {
            gameOverListener.onGameOver();
        }
    }

    // ========================
    // DRAW LOOP
    // ========================
    public void draw(Canvas canvas) {
        if (canvas == null) return;

        ensureSize();

        canvas.drawColor(Color.BLACK);

        int width = numColumns * cellSize;
        int height = numRows * cellSize;

        // grid
        for (int i = 0; i <= numColumns; i++) {
            canvas.drawLine(i * cellSize, 0, i * cellSize, height, gridPaint);
        }
        for (int j = 0; j <= numRows; j++) {
            canvas.drawLine(0, j * cellSize, width, j * cellSize, gridPaint);
        }

        // snake
        for (Point p : snake) {
            float left = p.x * cellSize;
            float top = p.y * cellSize;
            float right = left + cellSize;
            float down = top + cellSize;

            canvas.drawRect(left, top, right, down, snakePaint);
        }

        // food
        if (food != null) {
            float left = food.x * cellSize;
            float top = food.y * cellSize;
            float right = left + cellSize;
            float down = top + cellSize;

            canvas.drawRect(left, top, right, down, foodPaint);
        }
    }
}