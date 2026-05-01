package com.example.demo.controller;

import com.example.demo.dto.LoveArrowsResultRequest;
import com.example.demo.dto.LoveArrowsResultResponse;
import com.example.demo.dto.SnakeResultRequest;
import com.example.demo.dto.SnakeResultResponse;
import com.example.demo.model.User;
import com.example.demo.service.GameService;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;
    private final UserService userService;

    public GameController(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    private User getCurrentUser(Authentication auth) {
        String email = auth.getName();

        Optional<User> userOpt = userService.findByEmail(email);

        return userOpt.orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/love-arrows/result")
    public ResponseEntity<LoveArrowsResultResponse> submitLoveArrowsResult(@RequestBody LoveArrowsResultRequest request,
            Authentication auth) {
        User currentUser = getCurrentUser(auth);

        LoveArrowsResultResponse resp = gameService.submitLoveArrowsResult(currentUser, request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/love-arrows/summary")
    public ResponseEntity<LoveArrowsResultResponse> getLoveArrowsSummary(Authentication auth) {
        User currentUser = getCurrentUser(auth);
        LoveArrowsResultResponse resp = gameService.getLoveArrowsSummary(currentUser);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/snake/result")
    public ResponseEntity<SnakeResultResponse> submitSnakeResult(@RequestBody SnakeResultRequest request,
            Authentication auth) {
        User currentUser = getCurrentUser(auth);
        SnakeResultResponse resp = gameService.submitSnakeResult(currentUser, request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/snake/summary")
    public ResponseEntity<SnakeResultResponse> getSnakeSummary(Authentication auth) {
        User currentUser = getCurrentUser(auth);
        SnakeResultResponse resp = gameService.getSnakeSummary(currentUser);
        return ResponseEntity.ok(resp);
    }
}