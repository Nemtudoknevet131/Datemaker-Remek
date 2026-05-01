package com.example.datemaker.model;

public class FacebookLoginRequest {
    private String accessToken;

    public FacebookLoginRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
