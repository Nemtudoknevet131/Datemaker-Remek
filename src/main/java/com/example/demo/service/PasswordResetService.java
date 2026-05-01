package com.example.demo.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void createAndSendResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email doesn't exist"));

        String token = UUID.randomUUID().toString();

        user.setPasswordResetToken(token);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        log.info("[RESET] userId={} token={} expiresAt={}", user.getId(), token, user.getPasswordResetExpiresAt());

        String link = "https://datemakerr.ddns.net/api/users/reset-password/page?token=" + token;

        String html = """
                <p>Hi %s,</p>
                <p>You requested a password reset for your DateMaker account.</p>
                <p>Click the button below to choose a new password:</p>
                <p>
                  <a href="%s"
                     style="display:inline-block;padding:10px 18px;
                            background-color:#9D4EDD;color:#ffffff;
                            text-decoration:none;border-radius:6px;">
                    Reset password
                  </a>
                </p>
                <p>This link will expire in 1 hour. If you didn't request this, you can safely ignore this email.</p>
                """.formatted(
                user.getFirstName() != null ? user.getFirstName() : "there",
                link);

        log.info("[RESET] Link: {}", link);
        emailService.sendHtml(user.getEmail(), "Reset your DateMaker password", html);
        log.info("[RESET] email sent to {}", user.getEmail());
    }

    public void resetPassword(String token, String newPassword) {
        log.info("[RESET_CONFIRM] incoming token='{}'", token);

        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired link"));

        log.info("[RESET_CONFIRM] found user id={} expiresAt={}",
                user.getId(), user.getPasswordResetExpiresAt());

        if (user.getPasswordResetExpiresAt() == null
                || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired link");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);

        userRepository.save(user);
        log.info("[RESET_CONFIRM] password updated for user {}", user.getId());
    }
}