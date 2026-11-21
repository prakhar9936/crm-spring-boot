package com.prakhar.crm.service;

import com.prakhar.crm.entity.Role;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.ForbiddenException;
import com.prakhar.crm.exception.ResourceNotFoundException;
import com.prakhar.crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User makeUser(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    @Test
    void demoteToEmployee_shouldChangeRole_whenTargetIsAnotherUser() {
        User target = makeUser(2L, "anita@example.com", Role.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        userService.demoteToEmployee(2L, "test@example.com");

        assertEquals(Role.EMPLOYEE, target.getRole());
        verify(userRepository).save(target);
    }

    @Test
    void demoteToEmployee_shouldThrowForbidden_whenDemotingSelf() {
        User self = makeUser(1L, "test@example.com", Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(self));

        assertThrows(ForbiddenException.class,
                () -> userService.demoteToEmployee(1L, "TEST@example.com"));

        assertEquals(Role.ADMIN, self.getRole());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void demoteToEmployee_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.demoteToEmployee(99L, "test@example.com"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void promoteToAdmin_shouldChangeRole() {
        User target = makeUser(2L, "anita@example.com", Role.EMPLOYEE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        userService.promoteToAdmin(2L);

        assertEquals(Role.ADMIN, target.getRole());
        verify(userRepository).save(target);
    }
}