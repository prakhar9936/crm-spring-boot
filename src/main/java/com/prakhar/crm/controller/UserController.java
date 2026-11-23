package com.prakhar.crm.controller;

import com.prakhar.crm.dto.UserResponse;
import com.prakhar.crm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(PageRequest.of(page, size)));
    }

@GetMapping("/me")
public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {

    UserResponse user = userService.getUserByEmail(authentication.getName());

    return ResponseEntity.ok(user);
}

    @PutMapping("/{id}/promote")
    public ResponseEntity<String> promoteToAdmin(@PathVariable Long id) {
        userService.promoteToAdmin(id);
        return ResponseEntity.ok("User promoted to ADMIN");
    }

    @PutMapping("/{id}/demote")
    public ResponseEntity<String> demoteToEmployee(@PathVariable Long id, Authentication authentication) {
        userService.demoteToEmployee(id, authentication.getName());
        return ResponseEntity.ok("User demoted to EMPLOYEE");
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok("User activated");
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id, Authentication authentication) {
        userService.deactivateUser(id, authentication.getName());
        return ResponseEntity.ok("User deactivated");
    }
}