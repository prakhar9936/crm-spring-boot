package com.prakhar.crm.repository;

import com.prakhar.crm.entity.Contract;
import com.prakhar.crm.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByClient(Client client);

    List<Contract> findByClientId(Long clientId);
}