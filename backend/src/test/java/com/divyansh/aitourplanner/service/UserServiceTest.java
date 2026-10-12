package com.divyansh.aitourplanner.service;

import com.divyansh.aitourplanner.User;
import com.divyansh.aitourplanner.UserRepository;
import com.divyansh.aitourplanner.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService();
        setField(userService, "userRepository", userRepository);
        setField(userService, "passwordEncoder", passwordEncoder);
    }

    @Test
    void registerUser_newEmail_savesUserWithHashedPassword() {
        // Given
        User inputUser = new User();
        inputUser.setUsername("john");
        inputUser.setEmail("john@example.com");
        inputUser.setPassword("plainPassword123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User saved = userService.registerUser(inputUser);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.getUsername()).isEqualTo("john");
        assertThat(saved.getPassword()).isNotEqualTo("plainPassword123");
        assertThat(passwordEncoder.matches("plainPassword123", saved.getPassword())).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_existingEmail_throwsIllegalArgumentException() {
        // Given
        User existing = new User();
        existing.setEmail("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existing));

        User inputUser = new User();
        inputUser.setUsername("john");
        inputUser.setEmail("john@example.com");
        inputUser.setPassword("password123");

        // When/Then
        assertThatThrownBy(() -> userService.registerUser(inputUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An account with this email already exists.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_validCredentials_returnsUser() {
        // Given
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("correctPassword"));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.authenticate("john@example.com", "correctPassword");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void authenticate_wrongPassword_returnsEmpty() {
        // Given
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("correctPassword"));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.authenticate("john@example.com", "wrongPassword");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void authenticate_nonexistentEmail_returnsEmpty() {
        // Given
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.authenticate("unknown@example.com", "anyPassword");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void authenticate_nullEmail_returnsEmpty() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate(null, "password");
        assertThat(result).isEmpty();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}