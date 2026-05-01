package com.example.datemaker.model;

public class SnakeResultRequest {
    private int score;
    private int coins;

    public SnakeResultRequest(int score, int coins) {
        this.score = score;
        this.coins = coins;
    }

    public int getScore() {
        return score;
    }

    public int getCoins() {
        return coins;
    }
}
