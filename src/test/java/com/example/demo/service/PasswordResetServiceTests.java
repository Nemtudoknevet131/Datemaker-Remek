package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void createAndSendResetToken_persistsTokenAndSendsEmail() {
        User user = new User();
        user.setId(5L);
        user.setFirstName("Anna");
        user.setEmail("anna@example.com");

        when(userRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.createAndSendResetToken("anna@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getPasswordResetToken()).isNotBlank();
        assertThat(saved.getPasswordResetExpiresAt()).isAfter(LocalDateTime.now().minusMinutes(1));

        verify(emailService).sendHtml(
                eq("anna@example.com"),
                eq("Reset your DateMaker password"),
                contains("Reset password"));
    }

    @Test
    void createAndSendResetToken_throwsWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.createAndSendResetToken("missing@example.com"));

        assertThat(ex.getMessage()).isEqualTo("Email doesn't exist");
        verify(emailService, never()).sendHtml(any(), any(), any());
    }

    @Test
    void resetPassword_updatesPasswordAndClearsResetFields_whenTokenIsValid() {
        User user = new User();
        user.setId(8L);
        user.setPasswordResetToken("valid-token");
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(30));

        when(userRepository.findByPasswordResetToken("valid-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded-secret");

        passwordResetService.resetPassword("valid-token", "new-secret");

        assertThat(user.getPassword()).isEqualTo("encoded-secret");
        assertThat(user.getPasswordResetToken()).isNull();
        assertThat(user.getPasswordResetExpiresAt()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_throwsWhenTokenDoesNotExist() {
        when(userRepository.findByPasswordResetToken("missing")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword("missing", "new-secret"));

        assertThat(ex.getMessage()).isEqualTo("Invalid or expired link");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_throwsWhenTokenExpired() {
        User user = new User();
        user.setPasswordResetToken("expired");
        user.setPasswordResetExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByPasswordResetToken("expired")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword("expired", "new-secret"));

        assertThat(ex.getMessage()).isEqualTo("Invalid or expired link");
        verify(userRepository, never()).save(any(User.class));
    }
}
