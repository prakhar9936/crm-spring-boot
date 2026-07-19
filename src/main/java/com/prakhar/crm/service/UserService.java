package com.prakhar.crm.service;

import com.prakhar.crm.entity.Role;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.ResourceNotFoundException;
import com.prakhar.crm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void promoteToAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }
}