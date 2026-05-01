package com.example.datemaker.model;

public class LoveArrowsResultRequest {
    private int score;
    private int coins;
    private boolean watchedDouble;

    public LoveArrowsResultRequest(int score, int coins, boolean watchedDouble) {
        this.score = score;
        this.coins = coins;
        this.watchedDouble = watchedDouble;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public boolean isWatchedDouble() {
        return watchedDouble;
    }

    public void setWatchedDouble(boolean watchedDouble) {
        this.watchedDouble = watchedDouble;
    }
}
