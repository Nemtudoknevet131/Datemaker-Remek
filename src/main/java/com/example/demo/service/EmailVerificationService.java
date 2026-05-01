package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.email.verification.code-ttl-minutes:10}")
    private int codeTtlMinutes;

    @Value("${app.email.verification.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    public EmailVerificationService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    private String genCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    @Transactional
    public void createAndSendCode(User user) {
        String code = genCode();

        System.out.println(code);

        user.setEmailVerificationToken(code);
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusMinutes(codeTtlMinutes));
        user.setEmailVerificationAttempts(0);
        userRepository.save(user);

        String html = """
                <p>Hi %s,</p>
                <p>Your DateMaker verification code is: <b style="font-size:18px;">%s</b></p>
                <p>This code expires in %d minutes.</p>
                """.formatted(
                user.getFirstName() != null ? user.getFirstName() : "there",
                code,
                codeTtlMinutes);

        try {
            emailService.sendHtml(
                    user.getEmail(),
                    "Verify your datemaker email.",
                    html);
        } catch (Exception e) {
            System.out.println("⚠ Email küldés nem sikerült: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (Boolean.TRUE.equals(user.isEmailVerified())) {
            return true;
        }

        if (user.getEmailVerificationToken() == null || user.getEmailVerificationExpiresAt() == null) {
            throw new IllegalArgumentException("No verification code found. Please request a new one.");
        }

        if (LocalDateTime.now().isAfter(user.getEmailVerificationExpiresAt())) {
            throw new IllegalArgumentException("Code expired");
        }

        user.setEmailVerificationAttempts(user.getEmailVerificationAttempts() + 1);

        if (!code.equals(user.getEmailVerificationToken())) {
            userRepository.save(user);
            return false;
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationAttempts(0);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public void resendCode(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (Boolean.TRUE.equals(user.isEmailVerified())) {
            return;
        }

        if (user.getEmailVerificationExpiresAt() != null) {
            LocalDateTime issuedAt = user.getEmailVerificationExpiresAt().minusMinutes(codeTtlMinutes);

            if (issuedAt.isAfter(LocalDateTime.now().minusSeconds(resendCooldownSeconds))) {
                throw new IllegalArgumentException("Please wait before requesting a new code.");
            }
        }

        createAndSendCode(user);
    }
}