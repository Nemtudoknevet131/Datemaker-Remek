package com.example.demo.dto;

import com.example.demo.model.User;

public record ContactInfoDto(
        String email,
        String phoneNumber) {
    public static ContactInfoDto from(User u) {
        return new ContactInfoDto(
                u.getEmail(),
                u.getPhoneNumber());
    }
}