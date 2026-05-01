package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.model.Message;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime createdAt;
    private boolean readFlag;

    public static MessageDto from(Message m) {
        return new MessageDto(
                m.getId(),
                m.getSender().getId(),
                m.getReceiver().getId(),
                m.getContent(),
                m.getCreatedAt(),
                m.isReadFlag());
    }
}