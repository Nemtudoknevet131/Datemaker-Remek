package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.dto.MessageDto;
import com.example.demo.model.User;
import com.example.demo.push.FireBaseManager;

import lombok.*;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final FireBaseManager fireBaseManager;

    public void sendChatMessage(User receiver, MessageDto dto) {
        if (receiver.getFcmToken() == null || receiver.getFcmToken().isBlank()) {
            return;
        }

        String title = "New Message";
        String body = dto.getContent();
        fireBaseManager.sendMessage(receiver.getFcmToken(), title, body, "CHAT", dto);
    }
}