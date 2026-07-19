package com.prakhar.crm.service;

import com.prakhar.crm.dto.RegisterRequest;
import com.prakhar.crm.dto.UserResponse;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.EmailAlreadyExistsException;
import com.prakhar.crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldCreateUser_whenEmailDoesNotExist() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setFirstName("Test");
        savedUser.setLastName("User");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.register(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}