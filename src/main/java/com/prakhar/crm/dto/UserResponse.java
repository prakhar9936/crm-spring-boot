package com.prakhar.crm.dto;

import com.prakhar.crm.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean active;

    public UserResponse(Long id, String email, String firstName, String lastName) {
        this(id, email, firstName, lastName, null, true);
    }
}