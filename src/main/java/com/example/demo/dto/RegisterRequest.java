package com.example.demo.dto;

import lombok.*;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private boolean adult;
    private String dateOfBirth;

    private String recaptchaToken;
}