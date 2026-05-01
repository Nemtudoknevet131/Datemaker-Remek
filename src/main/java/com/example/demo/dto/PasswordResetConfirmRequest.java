package com.example.demo.dto;

public record PasswordResetConfirmRequest(
        String token,
        String newPassword) {

}
