package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    @NotNull
    @Email
    private String email;

    @JsonIgnore
    private String password;

    private boolean isAdult;

    private LocalDate dateOfBirth;

    @OneToOne
    @JoinColumn(name = "partner_id")
    @JsonIgnoreProperties({ "partner" })
    private User partner;

    private LocalDate relationshipStartDate;

    private String fcmToken;

    private boolean emailVerified = false;

    @JsonIgnore
    private String emailVerificationToken;

    @JsonIgnore
    private LocalDateTime emailVerificationExpiresAt;

    @JsonIgnore
    private Integer emailVerificationAttempts = 0;

    private String avatarUrl;

    private String phoneNumber;

    @JsonIgnore
    private String phoneToVerify;

    @JsonIgnore
    private String phoneVerificationCode;

    @JsonIgnore
    private LocalDateTime phoneVerificationExpiresAt;

    @JsonIgnore
    private Integer phoneVerificationAttempts = 0;

    @JsonIgnore
    private String passwordResetToken;

    @JsonIgnore
    private LocalDateTime passwordResetExpiresAt;

    private boolean premium;
}