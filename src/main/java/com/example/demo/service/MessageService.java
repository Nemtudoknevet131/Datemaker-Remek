package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.MessageDto;
import com.example.demo.dto.SendMessageRequest;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final FcmService fcmService;

    public List<MessageDto> getConversation(Long userId, Long partnerId) {
        return messageRepository.findConversation(userId, partnerId)
                .stream()
                .map(MessageDto::from)
                .toList();
    }

    public MessageDto sendMessage(Long senderId, SendMessageRequest request) {
        User sender = userService.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User receiver = userService.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(request.getContent());
        msg.setCreatedAt(LocalDateTime.now());
        msg.setReadFlag(false);

        Message saved = messageRepository.save(msg);
        MessageDto dto = MessageDto.from(saved);

        try {
            fcmService.sendChatMessage(receiver, dto);
        } catch (Exception e) {
            log.error("Failed to send FCM notification", e);
        }

        return dto;
    }
}