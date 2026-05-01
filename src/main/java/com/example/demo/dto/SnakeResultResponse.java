package com.example.demo.dto;

public record SnakeResultResponse(
        int score,
        int myHighScore,
        Integer partnerHighScore,
        int myCoins,
        Integer partnerCoins,
        Integer coupleCoins) {

}