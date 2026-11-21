package com.prakhar.crm.repository;

import com.prakhar.crm.entity.Client;
import com.prakhar.crm.entity.Contract;
import com.prakhar.crm.entity.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByClient(Client client);

    List<Contract> findByClientId(Long clientId);

    boolean existsByClientId(Long clientId);

    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);
}