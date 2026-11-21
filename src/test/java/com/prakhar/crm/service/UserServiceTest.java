package com.prakhar.crm.service;

import com.prakhar.crm.dto.UserResponse;
import com.prakhar.crm.entity.Role;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.ForbiddenException;
import com.prakhar.crm.exception.ResourceNotFoundException;
import com.prakhar.crm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void promoteToAdmin_shouldSetRoleAdmin() {
        User user = new User();
        user.setId(1L);
        user.setEmail("emp@example.com");
        user.setRole(Role.EMPLOYEE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.promoteToAdmin(1L);

        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void promoteToAdmin_shouldThrowNotFound_whenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.promoteToAdmin(99L));
    }

    @Test
    void demoteToEmployee_shouldSetRoleEmployee() {
        User user = new User();
        user.setId(2L);
        user.setEmail("other@example.com");
        user.setRole(Role.ADMIN);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        userService.demoteToEmployee(2L, "admin@example.com");

        assertEquals(Role.EMPLOYEE, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void demoteToEmployee_shouldThrowForbidden_whenDemotingSelf() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class,
                () -> userService.demoteToEmployee(1L, "admin@example.com"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_shouldReturnPagedUserResponses() {
        User u1 = new User();
        u1.setId(1L);
        u1.setEmail("admin@example.com");
        u1.setFirstName("Admin");
        u1.setLastName("User");
        u1.setRole(Role.ADMIN);
        u1.setActive(true);

        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(u1)));

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("admin@example.com", result.getContent().get(0).getEmail());
        assertEquals(Role.ADMIN, result.getContent().get(0).getRole());
        assertTrue(result.getContent().get(0).isActive());
    }

    @Test
    void activateUser_shouldSetActiveTrue() {
        User user = new User();
        user.setId(2L);
        user.setEmail("emp@example.com");
        user.setActive(false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        userService.activateUser(2L);

        assertTrue(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_shouldSetActiveFalse() {
        User user = new User();
        user.setId(2L);
        user.setEmail("emp@example.com");
        user.setActive(true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        userService.deactivateUser(2L, "admin@example.com");

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_shouldThrowForbidden_whenDeactivatingSelf() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class,
                () -> userService.deactivateUser(1L, "admin@example.com"));
        verify(userRepository, never()).save(any());
    }
}