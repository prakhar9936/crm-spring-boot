package com.prakhar.crm.controller;

import com.prakhar.crm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication) {
        return "You are logged in as: " + authentication.getName();
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
}
