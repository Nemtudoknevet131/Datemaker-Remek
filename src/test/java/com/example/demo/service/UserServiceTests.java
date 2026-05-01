package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    EmailVerificationService emailVerificationService;

    @Mock
    SmsService smsService;

    @InjectMocks
    UserService userService;

    // Getting all users from repo
    @Test
    void getAllUsers_returnsAllUsersFromRepository() {
        User user1 = new User();
        user1.setId(1L);
        user1.setFirstName("RandomDude");

        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("RandomDude2");

        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll())
                .thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2).containsExactly(user1, user2);

        verify(userRepository).findAll();
    }

    // Finding id when user is present
    @Test
    void findById_returnsCertainUserById_whenPresent() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("RandomDude");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);

        assertThat(result)
                .isPresent()
                .contains(user);

        verify(userRepository).findById(1L);
    }

    // Finding id when user is not present
    @Test
    void findById_returnsCertainUserById_whenNotPresent() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.findById(1L);

        assertThat(result).isEmpty();

        verify(userRepository).findById(1L);
    }

    // Deleting user by id when user is present
    @Test
    void deleteById_deletesCertainUserById_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteById(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    // Deleting user by id when user is not present
    @Test
    void deleteById_deletesCertainUserById_whenDoesntExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.deleteById(1L);
        });

        assertEquals("User not found with id: 1", ex.getMessage());

        verify(userRepository, never()).deleteById(1L);
    }

    // Finding user by id when user is present
    @Test
    void findByEmail_returnCertainUserByEmail_whenPresent() {
        User user = new User();
        user.setId(1L);
        user.setEmail("example@gmail.com");

        when(userRepository.findByEmail("example@gmail.com"))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("example@gmail.com");

        assertThat(result).isPresent().contains(user);

        verify(userRepository).findByEmail("example@gmail.com");
    }

    // Finding user by id when user is not present
    @Test
    void findByEmail_returnCertainUserByEmail_whenNotPresent() {
        when(userRepository.findByEmail("example@gmail.com"))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("example@gmail.com");

        assertThat(result).isEmpty();
    }

    // Checking if user exists by id when present
    @Test
    void existsById_returnUserById_whenPresent() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean result = userService.existsById(1L);

        assertTrue(result);
        verify(userRepository).existsById(1L);
    }

    // Checking if user exists by id when not present
    @Test
    void existsById_returnUserById_whenNotPresent() {
        when(userRepository.existsById(1L)).thenReturn(false);

        boolean result = userService.existsById(1L);

        assertFalse(result);
        verify(userRepository).existsById(1L);
    }

    // Seeing if user exists by username when is present
    @Test
    void existsByUsername_returnsCertainUserByUsername_whenPresent() {
        when(userRepository.existsByUsername("RandomDude")).thenReturn(true);

        boolean result = userService.existsByUsername("RandomDude");

        assertTrue(result);
        verify(userRepository).existsByUsername("RandomDude");
    }

    // Seeing if user exists by username when is not present
    @Test
    void existsByUsername_returnsCertainUserByUsername_whenNotPresent() {
        when(userRepository.existsByUsername("RandomDude")).thenReturn(false);

        boolean result = userService.existsByUsername("RandomDude");

        assertFalse(result);
        verify(userRepository).existsByUsername("RandomDude");
    }

    // Registering user when adult
    @Test
    void registerUser_whenAdult_setsAdultTrueAndEncodesPassword() {
        User user = new User();
        user.setDateOfBirth(LocalDate.now().minusYears(20));
        user.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("encodedPassword");

        User savedFromRepo = new User();
        savedFromRepo.setId(1L);
        when(userRepository.save(any(User.class)))
                .thenReturn(savedFromRepo);

        User result = userService.registerUser(user);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertTrue(savedUser.isAdult());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(1L, result.getId());
        verify(emailVerificationService).createAndSendCode(savedFromRepo);
    }

    // Registering user when minor
    @Test
    void registerUser_whenMinor_setsAdultFalseAndEncodesPassword() {
        User user = new User();
        user.setDateOfBirth(LocalDate.now().minusYears(16));
        user.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("encodedPassword");

        User savedFromRepo = new User();
        savedFromRepo.setId(2L);
        when(userRepository.save(any(User.class)))
                .thenReturn(savedFromRepo);

        User result = userService.registerUser(user);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertFalse(savedUser.isAdult());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(2L, result.getId());
        verify(emailVerificationService).createAndSendCode(savedFromRepo);
    }

    @Test
    void checkPassword_returnsPassword_whenMatches() {
        User user = new User();
        user.setPassword("encodedPassword");

        when(passwordEncoder.matches("rawPassword", "encodedPassword"))
                .thenReturn(true);

        boolean result = userService.checkPassword(user, "rawPassword");

        assertTrue(result);
        verify(passwordEncoder).matches("rawPassword", "encodedPassword");
    }

    @Test
    void checkPassword_doesntReturnPassword_whenDoesntMatch() {
        User user = new User();
        user.setPassword("encodedPassword");

        when(passwordEncoder.matches("rawPassword", "encodedPassword"))
                .thenReturn(false);

        boolean result = userService.checkPassword(user, "rawPassword");

        assertFalse(result);
        verify(passwordEncoder).matches("rawPassword", "encodedPassword");
    }

    @Test
    void updateUserEntity_updatesUserEntityAndEncodesNewPassword_whenPasswordNotEmpty() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("OldFN");
        existingUser.setLastName("OldLN");
        existingUser.setUsername("OldUser");
        existingUser.setEmail("old@gmail.com");
        existingUser.setDateOfBirth(LocalDate.now().minusYears(16));
        existingUser.setAdult(false);
        existingUser.setPassword("oldEncodedPassword");

        User userDetails = new User();
        userDetails.setId(1L);
        userDetails.setFirstName("NewFN");
        userDetails.setLastName("NewLN");
        userDetails.setUsername("NewUser");
        userDetails.setEmail("new@gmail.com");
        userDetails.setDateOfBirth(LocalDate.now().minusYears(20));
        userDetails.setAdult(true);
        userDetails.setPassword("newPlainPassword");

        when(userRepository.save(existingUser)).thenReturn(existingUser);

        when(passwordEncoder.encode("newPlainPassword"))
                .thenReturn("newEncodedPassword");

        User result = userService.updateUserEntity(existingUser, userDetails);

        assertEquals("NewFN", existingUser.getFirstName());
        assertEquals("NewLN", existingUser.getLastName());
        assertEquals("NewUser", existingUser.getUsername());
        assertEquals("new@gmail.com", existingUser.getEmail());
        assertEquals("newEncodedPassword", existingUser.getPassword());

        assertTrue(existingUser.isAdult());
        assertEquals(existingUser.getDateOfBirth(), userDetails.getDateOfBirth());
        assertEquals(existingUser, result);

        verify(passwordEncoder).encode("newPlainPassword");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserEntity_updatesUserEntityAndDoesntEncodesNewPassword_whenPasswordNull() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("OldFN");
        existingUser.setLastName("OldLN");
        existingUser.setUsername("OldUser");
        existingUser.setEmail("old@gmail.com");
        existingUser.setDateOfBirth(LocalDate.now().minusYears(16));
        existingUser.setAdult(false);
        existingUser.setPassword("oldEncodedPassword");

        User userDetails = new User();
        userDetails.setId(1L);
        userDetails.setFirstName("NewFN");
        userDetails.setLastName("NewLN");
        userDetails.setUsername("NewUser");
        userDetails.setEmail("new@gmail.com");
        userDetails.setDateOfBirth(LocalDate.now().minusYears(20));
        userDetails.setAdult(true);
        userDetails.setPassword(null);

        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User result = userService.updateUserEntity(existingUser, userDetails);

        assertEquals("NewFN", existingUser.getFirstName());
        assertEquals("NewLN", existingUser.getLastName());
        assertEquals("NewUser", existingUser.getUsername());
        assertEquals("new@gmail.com", existingUser.getEmail());
        assertEquals("oldEncodedPassword", existingUser.getPassword());
        assertEquals(existingUser, result);

        assertTrue(existingUser.isAdult());
        assertEquals(existingUser.getDateOfBirth(), userDetails.getDateOfBirth());

        verify(userRepository).save(existingUser);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUserEntity_updatesUserEntityAndDoesntEncodesNewPassword_whenPasswordEmpty() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("OldFN");
        existingUser.setLastName("OldLN");
        existingUser.setUsername("OldUser");
        existingUser.setEmail("old@gmail.com");
        existingUser.setDateOfBirth(LocalDate.now().minusYears(16));
        existingUser.setAdult(false);
        existingUser.setPassword("oldEncodedPassword");

        User userDetails = new User();
        userDetails.setId(1L);
        userDetails.setFirstName("NewFN");
        userDetails.setLastName("NewLN");
        userDetails.setUsername("NewUser");
        userDetails.setEmail("new@gmail.com");
        userDetails.setDateOfBirth(LocalDate.now().minusYears(20));
        userDetails.setAdult(true);
        userDetails.setPassword("");

        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User result = userService.updateUserEntity(existingUser, userDetails);

        assertEquals("NewFN", existingUser.getFirstName());
        assertEquals("NewLN", existingUser.getLastName());
        assertEquals("NewUser", existingUser.getUsername());
        assertEquals("new@gmail.com", existingUser.getEmail());
        assertEquals("oldEncodedPassword", existingUser.getPassword());
        assertEquals(existingUser, result);

        assertTrue(existingUser.isAdult());
        assertEquals(existingUser.getDateOfBirth(), userDetails.getDateOfBirth());

        verify(userRepository).save(existingUser);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void addPartner_connectsUsersBidirectionallyAndPersistsBoth() {
        User user = new User();
        user.setId(1L);
        user.setEmail("me@example.com");

        User partner = new User();
        partner.setId(2L);
        partner.setEmail("partner@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("partner@example.com")).thenReturn(Optional.of(partner));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean success = userService.addPartner(1L, "partner@example.com");

        assertTrue(success);
        assertEquals(partner, user.getPartner());
        assertEquals(user, partner.getPartner());
        verify(userRepository).save(user);
        verify(userRepository).save(partner);
    }

    @Test
    void removePartner_unlinksBothUsersAndPersistsBoth() {
        User partner = new User();
        partner.setId(2L);

        User user = new User();
        user.setId(1L);
        user.setPartner(partner);

        partner.setPartner(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(partner));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean success = userService.removePartner(1L);

        assertTrue(success);
        assertEquals(null, user.getPartner());
        assertEquals(null, partner.getPartner());
        verify(userRepository).save(partner);
        verify(userRepository).save(user);
    }
}