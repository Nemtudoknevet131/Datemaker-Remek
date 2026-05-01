package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AiChatRequestDto;
import com.example.demo.dto.AiChatResponseDto;
import com.example.demo.service.AiChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService aiChatService;

    @PostMapping("/date-planner")
    public AiChatResponseDto dto(@RequestBody AiChatRequestDto request) {
        String reply = aiChatService.generateDateIdea(request.getUserId(), request.getMessage());
        return new AiChatResponseDto(reply);
    }
}