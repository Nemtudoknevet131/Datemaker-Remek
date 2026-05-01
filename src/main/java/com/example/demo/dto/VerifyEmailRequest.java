package com.example.demo.dto;

import lombok.Data;

@Data
public class VerifyEmailRequest {
    private String email;
    private String code;
}