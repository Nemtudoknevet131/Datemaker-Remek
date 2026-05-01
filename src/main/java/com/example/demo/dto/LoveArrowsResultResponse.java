package com.example.demo.dto;

public record LoveArrowsResultResponse(
        int lastScore,
        int myHighScore,
        Integer partnerHighScore,
        int myCoins,
        Integer partnerCoins,
        Integer coupleCoins) {
}