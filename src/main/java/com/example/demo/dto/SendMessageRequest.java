package com.example.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    private Long receiverId;
    private String content;
}