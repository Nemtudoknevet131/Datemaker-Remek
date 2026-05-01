package com.example.demo.repository;

import com.example.demo.model.GameStats;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameStatsRepository extends JpaRepository<GameStats, Long> {
    Optional<GameStats> findByUserAndGameName(User user, String gameName);

    List<GameStats> findByUserInAndGameName(List<User> users, String gameName);
}