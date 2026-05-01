package com.example.demo.dto;

import java.time.LocalDate;

import com.example.demo.model.User;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String email,
        boolean emailVerified,
        boolean adult,
        LocalDate dateOfBirth,
        Long partnerId,
        String avatarUrl,
        boolean premium) {
    public static UserDto from(User u) {
        return new UserDto(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getUsername(),
                u.getEmail(),
                u.isEmailVerified(),
                u.isAdult(),
                u.getDateOfBirth(),
                u.getPartner() != null ? u.getPartner().getId() : null,
                u.getAvatarUrl(),
                u.isPremium());
    }
}