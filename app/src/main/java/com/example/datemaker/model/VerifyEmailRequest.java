package com.example.datemaker.model;

public class VerifyEmailRequest {
    private String email;
    private String code;

    public VerifyEmailRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }
}
