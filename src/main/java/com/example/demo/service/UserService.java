package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private SmsService smsService;

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Finding by id
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Deleting by id
    public void deleteById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    // Finding by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Does it exist by id
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    // Does it exist by email
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Does it exist by username
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Registering new user
    public User registerUser(User user) {
        LocalDate today = LocalDate.now();
        Period age = Period.between(user.getDateOfBirth(), today);
        user.setAdult(age.getYears() >= 18);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User saved = userRepository.save(user);

        emailVerificationService.createAndSendCode(saved);

        return saved;
    }

    // Checking matching password
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // Saving updated users
    public User save(User user) {
        return userRepository.save(user);
    }

    public User updateUserEntity(User existingUser, User userDetails) {
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setEmail(userDetails.getEmail());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        existingUser.setAdult(userDetails.isAdult());
        existingUser.setDateOfBirth(userDetails.getDateOfBirth());

        return userRepository.save(existingUser);
    }

    // Partner adding system
    public boolean addPartner(Long userId, String partnerEmail) {
        Optional<User> userOpt = findById(userId);
        Optional<User> partnerOpt = findByEmail(partnerEmail);

        if (userOpt.isEmpty() || partnerOpt.isEmpty())
            return false;

        User user = userOpt.get();
        User partner = partnerOpt.get();

        if (user.getPartner() != null || partner.getPartner() != null)
            return false;

        user.setPartner(partner);
        partner.setPartner(user);

        userRepository.save(user);
        userRepository.save(partner);

        return true;
    }

    public boolean removePartner(Long userId) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        User partner = user.getPartner();

        if (partner == null) {
            return true;
        }

        Optional<User> partnerOpt = findById(partner.getId());
        if (partnerOpt.isPresent()) {
            User partnerEntity = partnerOpt.get();
            partnerEntity.setPartner(null);
            userRepository.save(partnerEntity);
        }

        user.setPartner(null);
        userRepository.save(user);

        return true;
    }

    public void updateFcmTokenByEmail(String email, String fcmToken) {
        Optional<User> userOpt = findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFcmToken(fcmToken);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with id: " + email);
        }

        System.out.println("Updating FCM token for user " + email + " -> " + fcmToken);
    }

    public void createAndSendPhoneCode(User user, String phoneNumber) {
        String code = String.format("%06d", new Random().nextInt(1_000_000));

        user.setPhoneToVerify(phoneNumber);
        user.setPhoneVerificationCode(code);
        user.setPhoneVerificationExpiresAt(LocalDateTime.now().plusMinutes(10));
        user.setPhoneVerificationAttempts(0);

        userRepository.save(user);

        String message = "Your DateMaker verification code is: " + code;
        smsService.sendSms(phoneNumber, message);
    }

    public User verifyPhoneCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getPhoneVerificationCode() == null || user.getPhoneVerificationExpiresAt() == null) {
            throw new IllegalStateException("No phone verification in progress");
        }

        if (user.getPhoneVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Verification code expired");
        }

        int attempts = user.getPhoneVerificationAttempts() != null ? user.getPhoneVerificationAttempts() : 0;
        if (attempts >= 5) {
            throw new IllegalStateException("Too many attempts");
        }

        if (!code.equals(user.getPhoneVerificationCode())) {
            user.setPhoneVerificationAttempts(attempts + 1);
            userRepository.save(user);
            throw new IllegalStateException("Invalid Code");
        }

        user.setPhoneNumber(user.getPhoneToVerify());
        user.setPhoneToVerify(null);
        user.setPhoneVerificationCode(null);
        user.setPhoneVerificationExpiresAt(null);
        user.setPhoneVerificationAttempts(0);

        return userRepository.save(user);
    }

    public void setRelationshipDate(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User partner = user.getPartner();

        if (partner == null) {
            throw new RuntimeException("User has no partner");
        }

        user.setRelationshipStartDate(date);
        partner.setRelationshipStartDate(date);

        userRepository.save(user);
        userRepository.save(partner);
    }

    public Optional<User> findByEmailOrUsername(String query) {
        if (query == null || query.isBlank())
            return Optional.empty();

        return userRepository.findByEmail(query)
                .or(() -> userRepository.findByUsername(query));
    }
}