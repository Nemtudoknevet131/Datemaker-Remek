package com.example.datemaker.model;

import com.google.gson.annotations.SerializedName;

public class LoveArrowsResultResponse {
    private int lastScore;

    @SerializedName("myHighScore")
    private int highScore;

    private Integer partnerHighScore;

    @SerializedName("myCoins")
    private int myCoins;

    private Integer partnerCoins;
    private Integer coupleCoins;

    public LoveArrowsResultResponse() {
    }

    public LoveArrowsResultResponse(int lastScore, int highScore, Integer partnerHighScore, int myCoins, Integer partnerCoins, Integer coupleCoins) {
        this.lastScore = lastScore;
        this.highScore = highScore;
        this.partnerHighScore = partnerHighScore;
        this.myCoins = myCoins;
        this.partnerCoins = partnerCoins;
        this.coupleCoins = coupleCoins;
    }

    public int getLastScore() {
        return lastScore;
    }

    public int getHighScore() {
        return highScore;
    }

    public Integer getPartnerHighScore() {
        return partnerHighScore;
    }

    public int getMyCoins() {
        return myCoins;
    }

    public Integer getPartnerCoins() {
        return partnerCoins;
    }

    public Integer getCoupleCoins() {
        return coupleCoins;
    }
}
