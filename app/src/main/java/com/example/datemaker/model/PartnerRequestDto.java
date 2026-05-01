package com.example.datemaker.model;

public class PartnerRequestDto {
    private Long id;
    private Long fromUserId;
    private long toUserId;
    private String status;
    private String createdAt;
    private String fromUserName;

    public Long getId() {
        return id;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public long getToUserId() {
        return toUserId;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getFromUserName() {
        return fromUserName;
    }
}
