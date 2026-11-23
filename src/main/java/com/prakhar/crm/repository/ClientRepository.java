
package com.prakhar.crm.repository;

import com.prakhar.crm.entity.Client;
import com.prakhar.crm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByAssignedUser(User user);

    Page<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable);

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);
}
