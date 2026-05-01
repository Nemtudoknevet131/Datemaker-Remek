package com.example.datemaker.model;

public class SendMessageRequest {
    private Long receiverId;
    private String content;

    public SendMessageRequest(Long receiverId, String content) {
        this.receiverId = receiverId;
        this.content = content;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
