package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.MessageDto;
import com.example.demo.dto.SendMessageRequest;
import com.example.demo.model.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @GetMapping("/{partnerId}")
    public ResponseEntity<List<MessageDto>> getConversation(@PathVariable Long partnerId, Authentication auth) {
        String email = auth.getName();
        User me = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        log.info("[/api/messages/{}] user={} ({})", partnerId, me.getId(), me.getEmail());

        List<MessageDto> messages = messageService.getConversation(me.getId(), partnerId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@RequestBody SendMessageRequest request, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();
        User me = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        log.info("[POST /api/messages] from={} to={}", me.getId(), request.getReceiverId());

        MessageDto dto = messageService.sendMessage(me.getId(), request);
        return ResponseEntity.ok(dto);
    }
}