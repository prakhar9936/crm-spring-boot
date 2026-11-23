
package com.prakhar.crm.service;

import com.prakhar.crm.dto.LoginRequest;
import com.prakhar.crm.dto.RegisterRequest;
import com.prakhar.crm.dto.UserResponse;
import com.prakhar.crm.entity.Role;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.EmailAlreadyExistsException;
import com.prakhar.crm.exception.ForbiddenException;
import com.prakhar.crm.exception.InvalidCredentialsException;
import com.prakhar.crm.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private static final String TEST_SETUP_KEY = "test-admin-setup-key";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(
                authService,
                "adminSetupKey",
                TEST_SETUP_KEY);
    }

    @Test
    void register_shouldCreateUser_whenEmailDoesNotExist() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setFirstName("Test");
        savedUser.setLastName("User");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request, TEST_SETUP_KEY);

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

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request, TEST_SETUP_KEY));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldMakeFirstUserAdmin() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("first@example.com");
        request.setPassword("password123");
        request.setFirstName("First");
        request.setLastName("User");

        when(userRepository.existsByEmail("first@example.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        authService.register(request, TEST_SETUP_KEY);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertEquals(Role.ADMIN, captor.getValue().getRole());
    }

    @Test
    void register_shouldMakeLaterUsersEmployees() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("second@example.com");
        request.setPassword("password123");
        request.setFirstName("Second");
        request.setLastName("User");

        when(userRepository.existsByEmail("second@example.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(1L);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        authService.register(request, null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertEquals(Role.EMPLOYEE, captor.getValue().getRole());
    }

    @Test
    void register_shouldRejectFirstUser_whenSetupKeyIsMissing() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("first@example.com");
        request.setPassword("password123");
        request.setFirstName("First");
        request.setLastName("User");

        when(userRepository.existsByEmail("first@example.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);

        assertThrows(ForbiddenException.class, () -> authService.register(request, null));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldRejectFirstUser_whenSetupKeyIsIncorrect() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("first@example.com");
        request.setPassword("password123");
        request.setFirstName("First");
        request.setLastName("User");

        when(userRepository.existsByEmail("first@example.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);

        assertThrows(ForbiddenException.class, () -> authService.register(request, "wrong-setup-key"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldThrowInvalidCredentials_whenPasswordIsWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.EMPLOYEE);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void login_shouldThrowForbidden_whenAccountIsInactive() {
        LoginRequest request = new LoginRequest();
        request.setEmail("anita@example.com");
        request.setPassword("secret123");

        User user = new User();
        user.setEmail("anita@example.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.EMPLOYEE);
        user.setActive(false);

        when(userRepository.findByEmail("anita@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashedPassword"))
                .thenReturn(true);

        assertThrows(ForbiddenException.class,
                () -> authService.login(request));
    }

    @Test
    void login_shouldThrowInvalidCredentials_whenInactiveUserUsesWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("anita@example.com");
        request.setPassword("wrongpass");

        User user = new User();
        user.setEmail("anita@example.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.EMPLOYEE);
        user.setActive(false);

        when(userRepository.findByEmail("anita@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashedPassword"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }
}
