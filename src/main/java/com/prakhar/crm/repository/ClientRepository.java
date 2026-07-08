package com.prakhar.crm.repository;

import com.prakhar.crm.entity.Client;
import com.prakhar.crm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByAssignedUser(User user);

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);
}