package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.dto.LoveArrowsResultRequest;
import com.example.demo.dto.LoveArrowsResultResponse;
import com.example.demo.dto.SnakeResultRequest;
import com.example.demo.dto.SnakeResultResponse;
import com.example.demo.model.GameStats;
import com.example.demo.model.User;
import com.example.demo.push.FireBaseManager;
import com.example.demo.repository.GameStatsRepository;
import com.example.demo.repository.UserRepository;

@Service
public class GameService {
    private static final String LOVE_ARROWS = "LOVE_ARROWS";
    private static final String SNAKE = "SNAKE";

    private final GameStatsRepository gameStatsRepository;
    private final UserRepository userRepository;
    private final FireBaseManager fireBaseManager;

    public GameService(GameStatsRepository gameStatsRepository, UserRepository userRepository,
            FireBaseManager fireBaseManager) {
        this.gameStatsRepository = gameStatsRepository;
        this.userRepository = userRepository;
        this.fireBaseManager = fireBaseManager;
    }

    private GameStats getOrCreateGameStats(User user, String gameName) {
        return gameStatsRepository.findByUserAndGameName(user, gameName)
                .orElseGet(() -> {
                    GameStats stats = new GameStats();
                    stats.setUser(user);
                    stats.setGameName(gameName);
                    stats.setBestScore(0);
                    stats.setTotalCoins(0);
                    return gameStatsRepository.save(stats);
                });
    }

    public LoveArrowsResultResponse submitLoveArrowsResult(User currentUser, LoveArrowsResultRequest req) {
        int score = req.score();
        int coinsToAdd = req.coins();

        GameStats myStats = getOrCreateGameStats(currentUser, LOVE_ARROWS);

        if (score > myStats.getBestScore()) {
            myStats.setBestScore(score);
        }

        myStats.setTotalCoins(myStats.getTotalCoins() + coinsToAdd);
        gameStatsRepository.save(myStats);

        User partner = currentUser.getPartner();
        GameStats partnerStats = null;

        if (partner != null) {
            partnerStats = gameStatsRepository.findByUserAndGameName(partner, LOVE_ARROWS)
                    .orElse(null);
        }

        Integer partnerHighScore = partnerStats != null ? partnerStats.getBestScore() : null;
        Integer partnerCoins = partnerStats != null ? partnerStats.getTotalCoins() : null;
        Integer coupleCoins = null;

        if (partnerStats != null) {
            coupleCoins = myStats.getTotalCoins() + partnerStats.getTotalCoins();
        }

        if (partner != null && partner.getFcmToken() != null && !partner.getFcmToken().isBlank()) {
            String title = "New game score";
            String body = "Your partner has scored " + score + " on the game Love Arrows!";
            fireBaseManager.sendMessage(partner.getFcmToken(), title, body, "GAME_SCORE");
        }

        return new LoveArrowsResultResponse(
                score,
                myStats.getBestScore(),
                partnerHighScore,
                myStats.getTotalCoins(),
                partnerCoins,
                coupleCoins);
    }

    public LoveArrowsResultResponse getLoveArrowsSummary(User currentUser) {
        GameStats myStats = gameStatsRepository.findByUserAndGameName(currentUser, LOVE_ARROWS)
                .orElse(null);

        User partner = currentUser.getPartner();
        GameStats partnerStats = null;

        if (partner != null) {
            partnerStats = gameStatsRepository.findByUserAndGameName(partner, LOVE_ARROWS)
                    .orElse(null);
        }

        int myHighScore = myStats != null ? myStats.getBestScore() : 0;
        int myCoins = myStats != null ? myStats.getTotalCoins() : 0;
        Integer partnerHighScore = partnerStats != null ? partnerStats.getBestScore() : null;
        Integer partnerCoins = partnerStats != null ? partnerStats.getTotalCoins() : null;
        Integer coupleCoins = null;

        if (partnerStats != null) {
            coupleCoins = myStats.getTotalCoins() + partnerStats.getTotalCoins();
        }

        return new LoveArrowsResultResponse(
                0,
                myHighScore,
                partnerHighScore,
                myCoins,
                partnerCoins,
                coupleCoins);
    }

    public SnakeResultResponse submitSnakeResult(User currentUser, SnakeResultRequest req) {
        int score = req.score();
        int coinsToAdd = req.coins();

        GameStats myStats = getOrCreateGameStats(currentUser, SNAKE);

        if (score > myStats.getBestScore()) {
            myStats.setBestScore(score);
        }

        myStats.setTotalCoins(myStats.getTotalCoins() + coinsToAdd);
        gameStatsRepository.save(myStats);

        User partner = currentUser.getPartner();
        GameStats partnerStats = null;

        if (partner != null) {
            partnerStats = gameStatsRepository.findByUserAndGameName(partner, SNAKE)
                    .orElse(null);
        }

        Integer partnerHighScore = partnerStats != null ? partnerStats.getBestScore() : null;
        Integer partnerCoins = partnerStats != null ? partnerStats.getTotalCoins() : null;
        Integer coupleCoins = null;

        if (partnerStats != null) {
            coupleCoins = myStats.getTotalCoins() + partnerStats.getTotalCoins();
        }

        if (partner != null && partner.getFcmToken() != null && !partner.getFcmToken().isBlank()) {
            String title = "New Snake score!";
            String body = "Your partner just scored " + score + " in Snake!";
            fireBaseManager.sendMessage(partner.getFcmToken(), title, body, "SNAKE_SCORE");
        }

        return new SnakeResultResponse(
                score,
                myStats.getBestScore(),
                partnerHighScore,
                myStats.getTotalCoins(),
                partnerCoins,
                coupleCoins);
    }

    public SnakeResultResponse getSnakeSummary(User currentUser) {
        GameStats myStats = gameStatsRepository.findByUserAndGameName(currentUser, SNAKE)
                .orElse(null);

        User partner = currentUser.getPartner();
        GameStats partnerStats = null;

        if (partner != null) {
            partnerStats = gameStatsRepository.findByUserAndGameName(partner, SNAKE)
                    .orElse(null);
        }

        int myHighScore = myStats != null ? myStats.getBestScore() : 0;
        int myCoins = myStats != null ? myStats.getTotalCoins() : 0;
        Integer partnerHighScore = partnerStats != null ? partnerStats.getBestScore() : null;
        Integer partnerCoins = partnerStats != null ? partnerStats.getTotalCoins() : null;
        Integer coupleCoins = null;

        if (partnerStats != null && myStats != null) {
            coupleCoins = myStats.getTotalCoins() + partnerStats.getTotalCoins();
        }

        return new SnakeResultResponse(
                0,
                myHighScore,
                partnerHighScore,
                myCoins,
                partnerCoins,
                coupleCoins);
    }
}