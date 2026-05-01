package com.example.datemaker.model;

public class Message {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private String createdAt;
    private boolean readFlag;

    public Long getId() {
        return id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isReadFlag() {
        return readFlag;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setReadFlag(boolean readFlag) {
        this.readFlag = readFlag;
    }
}
