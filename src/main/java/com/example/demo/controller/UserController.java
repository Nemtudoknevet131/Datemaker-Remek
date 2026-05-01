package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.ContactInfoDto;
import com.example.demo.dto.UserDto;
import com.example.demo.dto.VerifyPhoneRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.PartnerSearchResponse;
import com.example.demo.dto.PasswordResetRequest;
import com.example.demo.dto.PhoneNumberRequest;
import com.example.demo.model.User;
import com.example.demo.repository.PartnerRequestRepository;
import com.example.demo.service.*;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private FacebookVerificationService facebookVerificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private RecaptchaService recaptchaService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GoogleVerificationService googleVerificationService;

    @Autowired
    private AvatarStorageService avatarStorageService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PartnerRequestRepository partnerRequestRepository;

    UserController(EmailService emailService, FacebookVerificationService facebookVerificationService) {
        this.emailService = emailService;
        this.facebookVerificationService = facebookVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody com.example.demo.dto.RegisterRequest request) {

        boolean captchaOk = recaptchaService.verify(request.getRecaptchaToken());
        if (!captchaOk) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid reCAPTCHA");
        }

        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use.");
        }

        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already in use.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setAdult(request.isAdult());

        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isBlank()) {
            user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth())); // "2025-11-06" format
        }

        User savedUser = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userService.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Invalid email or password.");
        }

        User user = userOptional.get();

        if (!userService.checkPassword(user, loginRequest.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Invalid email or password.");
        }

        if (!user.isEmailVerified()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Email is not verified.");
        }

        String token = jwtService.generateToken(user.getEmail());
        AuthResponse authResponse = new AuthResponse(token, UserDto.from(user));

        return ResponseEntity.ok(authResponse);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream().map(UserDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> userToUpdate = userService.findById(id);

        if (userToUpdate.isPresent()) {
            User existingUser = userToUpdate.get();
            existingUser.setFirstName(userDetails.getFirstName());
            existingUser.setLastName(userDetails.getLastName());
            existingUser.setUsername(userDetails.getUsername());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setPassword(userService.encodePassword(userDetails.getPassword()));
            existingUser.setAdult(userDetails.isAdult());
            existingUser.setDateOfBirth(userDetails.getDateOfBirth());

            User updateUser = userService.save(existingUser);
            return ResponseEntity.ok(updateUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.existsById(id)) {
            userService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();
        user.setPassword(null);

        return ResponseEntity.ok(UserDto.from(userOpt.get()));
    }

    @GetMapping("/search-partner")
    public ResponseEntity<?> searchPartner(@RequestParam Long currentUserId, @RequestParam String query) {
        var currentOpt = userService.findById(currentUserId);

        if (currentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Current user not found");
        }

        var targetOpt = userService.findByEmailOrUsername(query);

        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        var current = currentOpt.get();
        var target = targetOpt.get();

        boolean alreadyPartnered = (current.getPartner() != null
                && current.getPartner().getId().equals(target.getId()));

        boolean pendingFromMe = partnerRequestRepository.existsByFromUserIdAndToUserIdAndStatus(current.getId(),
                target.getId(), "PENDING");

        PartnerSearchResponse resp = new PartnerSearchResponse(
                UserDto.from(target),
                alreadyPartnered,
                pendingFromMe);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<?> saveFcmToken(@RequestParam String token, Authentication authentication) {
        String email = authentication.getName();
        userService.updateFcmTokenByEmail(email, token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody com.example.demo.dto.VerifyEmailRequest request) {
        try {
            boolean ok = emailVerificationService.verifyCode(request.getEmail(), request.getCode());
            if (ok)
                return ResponseEntity.ok("Email verified.");
            return ResponseEntity.badRequest().body("Invalid code.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@RequestBody com.example.demo.dto.ResendCodeRequest request) {
        try {
            emailVerificationService.resendCode(request.getEmail());
            return ResponseEntity.ok().body("New code sent.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody com.example.demo.dto.GoogleLoginRequest request) {
        try {
            var googleInfo = googleVerificationService.verify(request.getIdToken());

            if (googleInfo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token");
            }

            String email = googleInfo.getEmail();
            String name = googleInfo.getName();

            Optional<User> userOpt = userService.findByEmail(email);

            if (userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use.");
            }

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(name);
            newUser.setEmailVerified(true);
            newUser.setAdult(false);

            User saved = userService.save(newUser);

            String token = jwtService.generateToken(saved.getEmail());

            AuthResponse authResponse = new com.example.demo.dto.AuthResponse(token,
                    com.example.demo.dto.UserDto.from(saved));

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Google login failed");
        }
    }

    @PostMapping("/google-auth")
    public ResponseEntity<?> googleAuth(@RequestBody com.example.demo.dto.GoogleLoginRequest request) {
        try {
            var googleInfo = googleVerificationService.verify(request.getIdToken());

            if (googleInfo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token");
            }

            String email = googleInfo.getEmail();
            String name = googleInfo.getName();

            Optional<User> userOpt = userService.findByEmail(email);

            String picture = googleInfo.getPicture();

            if (userOpt.isPresent()) {
                User existing = userOpt.get();
                if ((existing.getAvatarUrl() == null || existing.getAvatarUrl().isBlank()) && picture != null) {
                    existing.setAvatarUrl(picture);
                    userService.save(existing);
                }
                String token = jwtService.generateToken(existing.getEmail());
                AuthResponse authResponse = new AuthResponse(token, UserDto.from(existing));
                return ResponseEntity.ok(authResponse);
            }

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(name);
            newUser.setEmailVerified(true);
            newUser.setAdult(false);
            if (picture != null)
                newUser.setAvatarUrl(picture);

            User saved = userService.save(newUser);

            String token = jwtService.generateToken(saved.getEmail());
            AuthResponse authResponse = new AuthResponse(token, UserDto.from(saved));
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Google auth failed");
        }
    }

    private static java.time.LocalDate parseFacebookBirthday(String bday) {
        if (bday == null || bday.isBlank())
            return null;

        try {
            var fmt = java.time.format.DateTimeFormatter.ofPattern("MM/dd/uuuu");
            return java.time.LocalDate.parse(bday, fmt);
        } catch (Exception ignored) {
            return null;
        }
    }

    @PostMapping("/facebook-auth")
    public ResponseEntity<?> facebookAuth(@RequestBody com.example.demo.dto.FacebookLoginRequest request) {
        try {
            log.info("[FB] /facebook-auth called");
            var fbInfo = facebookVerificationService.verify(request.getAccessToken());

            if (fbInfo == null) {
                log.warn("[FB] verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid facebook login");
            }

            String email = (fbInfo.email() != null && !fbInfo.email().isBlank())
                    ? fbInfo.email()
                    : fbInfo.id() + "@facebook.local";
            log.info("[FB] resolved email={}", email);

            Optional<User> userOpt = userService.findByEmail(email);

            String fbPic = facebookVerificationService.getProfilePictureUrl(request.getAccessToken());

            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
                if ((user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) && fbPic != null) {
                    user.setAvatarUrl(fbPic);
                    userService.save(user);
                }
                log.info("[FB] existing user id={} email={}", user.getId(), user.getEmail());
            } else {
                user = new User();
                user.setEmail(email);

                String name = fbInfo.name() != null ? fbInfo.name() : "Facebook User";
                String[] parts = name.split("\\s+", 2);
                user.setFirstName(parts[0]);
                if (parts.length > 1)
                    user.setLastName(parts[1]);

                var dob = parseFacebookBirthday(fbInfo.birthday());
                if (dob != null) {
                    user.setDateOfBirth(dob);
                    var eighteenAgo = java.time.LocalDate.now().minusYears(18);
                    user.setAdult(!dob.isAfter(eighteenAgo));
                } else {
                    user.setAdult(false);
                }

                if (fbPic != null)
                    user.setAvatarUrl(fbPic);

                user.setEmailVerified(true);
                user = userService.save(user);

                log.info("[FB] created user id={} email={} firstName={} lastName={}",
                        user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
            }

            String token = jwtService.generateToken(user.getEmail());
            log.info("[FB] issuing JWT for email={}", user.getEmail());

            AuthResponse authResponse = new AuthResponse(token, UserDto.from(user));
            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            log.error("[FB] facebookAuth failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Facebook auth failed");
        }
    }

    @PatchMapping("/me/avatar-url")
    public ResponseEntity<?> setAvatarUrl(@RequestBody java.util.Map<String, String> body, Authentication auth) {
        String email = auth.getName();
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body("Missing url");
        }

        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        User user = userOpt.get();
        user.setAvatarUrl(url);
        userService.save(user);

        return ResponseEntity.ok(UserDto.from(user));
    }

    @PostMapping(value = "/me/avatar-upload", consumes = { "multipart/form-data" })
    public ResponseEntity<?> uploadAvatar(@RequestPart("file") MultipartFile file, Authentication auth) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }

        String email = auth.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");

        try {
            String url = avatarStorageService.save(file);
            User user = userOpt.get();
            user.setAvatarUrl(url);
            userService.save(user);
            return ResponseEntity.ok(UserDto.from(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }

    @GetMapping("/me/contact")
    public ResponseEntity<?> getMyContact(Authentication auth) {
        String email = auth.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(ContactInfoDto.from(userOpt.get()));
    }

    @PatchMapping("/me/contact")
    public ResponseEntity<?> updateMyContact(@RequestBody ContactInfoDto contact, Authentication auth) {
        String email = auth.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();

        if (contact.email() != null && !contact.email().isBlank()) {
            user.setEmail(contact.email());
        }

        user = userService.save(user);
        return ResponseEntity.ok(ContactInfoDto.from(user));
    }

    @PostMapping("/me/phone/send-code")
    public ResponseEntity<?> sendPhoneVerificationCode(@RequestBody PhoneNumberRequest request, Authentication auth) {
        String email = auth.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }

        try {
            userService.createAndSendPhoneCode(userOpt.get(), request.phoneNumber());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to send phone verification SMS to {} for user {}",
                    request.phoneNumber(), email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send SMS");
        }
    }

    @PostMapping("/me/phone/verify")
    public ResponseEntity<?> verifyPhone(@RequestBody VerifyPhoneRequest request, Authentication auth) {
        String email = auth.getName();

        try {
            User updated = userService.verifyPhoneCode(email, request.code());
            return ResponseEntity.ok(ContactInfoDto.from(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        String email = auth.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(UserDto.from(userOpt.get()));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateMe(@RequestBody Map<String, String> body, Authentication auth) {
        String email = auth.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();

        if (body.containsKey("firstName")) {
            user.setFirstName(body.get("firstName"));
        }

        if (body.containsKey("lastName")) {
            user.setLastName(body.get("lastName"));
        }

        User saved = userService.save(user);
        return ResponseEntity.ok(UserDto.from(saved));
    }

    @PostMapping("/reset-password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        try {
            passwordResetService.createAndSendResetToken(request.email());
            return ResponseEntity.ok("Reset email sent.");
        } catch (IllegalArgumentException e) {
            if ("Email doesn't exist".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email doesn't exist");
            }

            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send request email");
        }
    }

    @PostMapping(value = "/reset-password/confirm", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> confirmResetPassword(@RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword) {
        try {
            passwordResetService.resetPassword(token, newPassword);
            String html = """
                    <html>
                      <body style="font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                                   background: #0b031a; color:#f8f0ff; display:flex;align-items:center;
                                   justify-content:center;min-height:100vh;margin:0;">
                        <div style="background:#130229;padding:28px 24px;border-radius:16px;
                                    max-width:420px;width:100%;text-align:center;
                                    box-shadow:0 20px 55px rgba(0,0,0,0.7);">
                          <h2 style="margin-top:0;margin-bottom:8px;">Password updated</h2>
                          <p style="color:#e0c3ff;font-size:14px;line-height:1.5;">
                            Your password has been changed successfully.<br/>
                            You can now close this window and sign in again in the DateMaker app.
                          </p>
                        </div>
                      </body>
                    </html>
                    """; // CHANGED
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html); // CHANGED
        } catch (IllegalArgumentException e) {
            String html = """
                    <html>
                      <body style="font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                                   background: #0b031a; color:#f8f0ff; display:flex;align-items:center;
                                   justify-content:center;min-height:100vh;margin:0;">
                        <div style="background:#130229;padding:28px 24px;border-radius:16px;
                                    max-width:420px;width:100%;text-align:center;
                                    box-shadow:0 20px 55px rgba(0,0,0,0.7);">
                          <h2 style="margin-top:0;margin-bottom:8px;">Link invalid or expired</h2>
                          <p style="color:#e0c3ff;font-size:14px;line-height:1.5;">
                            This reset link is no longer valid.<br/>
                            Please go back to the DateMaker app and request a new password reset email.
                          </p>
                        </div>
                      </body>
                    </html>
                    """;
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        }
    }

    @PatchMapping("/me/subscription")
    public ResponseEntity<?> updateSubscription(@RequestBody Map<String, Boolean> body, Authentication auth) {
        String email = auth.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Boolean premium = body.get("premium");
        if (premium == null) {
            return ResponseEntity.badRequest().body("Missing 'premium' flag");
        }

        User user = userOpt.get();
        user.setPremium(premium);
        userService.save(user);

        return ResponseEntity.ok(UserDto.from(user));
    }
}