package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.PasswordResetRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserDto;
import com.example.demo.model.User;
import com.example.demo.repository.PartnerRequestRepository;
import com.example.demo.service.AvatarStorageService;
import com.example.demo.service.EmailService;
import com.example.demo.service.EmailVerificationService;
import com.example.demo.service.FacebookVerificationService;
import com.example.demo.service.GoogleVerificationService;
import com.example.demo.service.JwtService;
import com.example.demo.service.PasswordResetService;
import com.example.demo.service.RecaptchaService;
import com.example.demo.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private FacebookVerificationService facebookVerificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private RecaptchaService recaptchaService;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleVerificationService googleVerificationService;

    @Mock
    private AvatarStorageService avatarStorageService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private PartnerRequestRepository partnerRequestRepository;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(emailService, facebookVerificationService);
        ReflectionTestUtils.setField(userController, "userService", userService);
        ReflectionTestUtils.setField(userController, "emailVerificationService", emailVerificationService);
        ReflectionTestUtils.setField(userController, "recaptchaService", recaptchaService);
        ReflectionTestUtils.setField(userController, "jwtService", jwtService);
        ReflectionTestUtils.setField(userController, "googleVerificationService", googleVerificationService);
        ReflectionTestUtils.setField(userController, "avatarStorageService", avatarStorageService);
        ReflectionTestUtils.setField(userController, "passwordResetService", passwordResetService);
        ReflectionTestUtils.setField(userController, "partnerRequestRepository", partnerRequestRepository);
    }

    @Test
    void createUser_returnsBadRequest_whenRecaptchaIsInvalid() {
        RegisterRequest req = new RegisterRequest();
        req.setRecaptchaToken("bad-token");

        when(recaptchaService.verify("bad-token")).thenReturn(false);

        ResponseEntity<?> response = userController.createUser(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid reCAPTCHA");
        verify(userService, never()).registerUser(any(User.class));
    }

    @Test
    void createUser_returnsBadRequest_whenEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setRecaptchaToken("ok-token");
        req.setEmail("taken@example.com");

        when(recaptchaService.verify("ok-token")).thenReturn(true);
        when(userService.existsByEmail("taken@example.com")).thenReturn(true);

        ResponseEntity<?> response = userController.createUser(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Error: Email is already in use.");
        verify(userService, never()).registerUser(any(User.class));
    }

    @Test
    void createUser_returnsCreatedAndUserDto_whenRequestIsValid() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Anna");
        req.setLastName("Kiss");
        req.setUsername("annak");
        req.setEmail("anna@example.com");
        req.setPassword("plain");
        req.setAdult(true);
        req.setDateOfBirth("2000-01-01");
        req.setRecaptchaToken("ok-token");

        User saved = new User();
        saved.setId(10L);
        saved.setFirstName("Anna");
        saved.setLastName("Kiss");
        saved.setUsername("annak");
        saved.setEmail("anna@example.com");
        saved.setAdult(true);
        saved.setDateOfBirth(LocalDate.of(2000, 1, 1));

        when(recaptchaService.verify("ok-token")).thenReturn(true);
        when(userService.existsByEmail("anna@example.com")).thenReturn(false);
        when(userService.existsByUsername("annak")).thenReturn(false);
        when(userService.registerUser(any(User.class))).thenReturn(saved);

        ResponseEntity<?> response = userController.createUser(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(UserDto.class);
        UserDto dto = (UserDto) response.getBody();
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.email()).isEqualTo("anna@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).registerUser(userCaptor.capture());
        assertThat(userCaptor.getValue().getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    void loginUser_returnsUnauthorized_whenEmailDoesNotExist() {
        LoginRequest req = new LoginRequest("missing@example.com", "secret");

        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.loginUser(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("Error: Invalid email or password.");
    }

    @Test
    void loginUser_returnsUnauthorized_whenPasswordIsWrong() {
        User user = new User();
        user.setEmail("user@example.com");

        LoginRequest req = new LoginRequest("user@example.com", "wrong");

        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userService.checkPassword(user, "wrong")).thenReturn(false);

        ResponseEntity<?> response = userController.loginUser(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("Error: Invalid email or password.");
    }

    @Test
    void loginUser_returnsForbidden_whenEmailNotVerified() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setEmailVerified(false);

        LoginRequest req = new LoginRequest("user@example.com", "secret");

        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userService.checkPassword(user, "secret")).thenReturn(true);

        ResponseEntity<?> response = userController.loginUser(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Email is not verified.");
    }

    @Test
    void loginUser_returnsJwtAndUserDto_whenCredentialsAreValid() {
        User user = new User();
        user.setId(42L);
        user.setEmail("user@example.com");
        user.setEmailVerified(true);

        LoginRequest req = new LoginRequest("user@example.com", "secret");

        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userService.checkPassword(user, "secret")).thenReturn(true);
        when(jwtService.generateToken("user@example.com")).thenReturn("jwt-token");

        ResponseEntity<?> response = userController.loginUser(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(AuthResponse.class);
        AuthResponse auth = (AuthResponse) response.getBody();
        assertThat(auth.token()).isEqualTo("jwt-token");
        assertThat(auth.user().id()).isEqualTo(42L);
        verify(jwtService).generateToken("user@example.com");
    }

    @Test
    void requestPasswordReset_returnsNotFound_whenEmailDoesNotExist() {
        doThrowIllegalArgument("Email doesn't exist");

        ResponseEntity<?> response = userController.requestPasswordReset(new PasswordResetRequest("missing@example.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Email doesn't exist");
    }

    @Test
    void requestPasswordReset_returnsOk_whenServiceCompletes() {
        ResponseEntity<?> response = userController.requestPasswordReset(new PasswordResetRequest("user@example.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Reset email sent.");
        verify(passwordResetService).createAndSendResetToken("user@example.com");
    }

    @Test
    void updateUser_updatesAndSavesUser_whenUserExists() {
        User existing = new User();
        existing.setId(1L);
        existing.setFirstName("Old");

        User details = new User();
        details.setFirstName("New");
        details.setLastName("Name");
        details.setUsername("newuser");
        details.setEmail("new@example.com");
        details.setPassword("plain");
        details.setAdult(true);
        details.setDateOfBirth(LocalDate.of(2001, 2, 3));

        when(userService.findById(1L)).thenReturn(Optional.of(existing));
        when(userService.encodePassword("plain")).thenReturn("encoded");
        when(userService.save(existing)).thenReturn(existing);

        ResponseEntity<User> response = userController.updateUser(1L, details);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(existing);
        assertThat(existing.getPassword()).isEqualTo("encoded");
        verify(userService).save(existing);
    }

    @Test
    void deleteUser_deletesFromDatabase_whenUserExists() {
        when(userService.existsById(9L)).thenReturn(true);

        ResponseEntity<Void> response = userController.deleteUser(9L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteById(9L);
    }

    @Test
    void deleteUser_returnsNotFound_whenUserMissing() {
        when(userService.existsById(9L)).thenReturn(false);

        ResponseEntity<Void> response = userController.deleteUser(9L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService, never()).deleteById(any(Long.class));
    }

    private void doThrowIllegalArgument(String message) {
        org.mockito.Mockito.doThrow(new IllegalArgumentException(message))
                .when(passwordResetService)
                .createAndSendResetToken(eq("missing@example.com"));
    }
}
