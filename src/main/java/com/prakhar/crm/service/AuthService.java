package com.prakhar.crm.service;

import com.prakhar.crm.dto.LoginRequest;
import com.prakhar.crm.dto.LoginResponse;
import com.prakhar.crm.dto.RegisterRequest;
import com.prakhar.crm.dto.UserResponse;
import com.prakhar.crm.entity.Role;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.EmailAlreadyExistsException;
import com.prakhar.crm.exception.ResourceNotFoundException;
import com.prakhar.crm.repository.UserRepository;
import com.prakhar.crm.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getEmail());
    }

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        user.setRole(Role.EMPLOYEE);

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName()
        );
    }
}