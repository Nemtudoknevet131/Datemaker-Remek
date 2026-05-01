package com.example.datemaker.model;

public class AuthResponse {
    private String token;
    private UserDto user;

    public String getToken() {
        return token;
    }

    public UserDto getUser() {
        return user;
    }
}
