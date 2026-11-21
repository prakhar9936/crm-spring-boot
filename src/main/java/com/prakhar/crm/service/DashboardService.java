package com.prakhar.crm.service;

import com.prakhar.crm.dto.DashboardResponse;
import com.prakhar.crm.entity.ContractStatus;
import com.prakhar.crm.repository.ClientRepository;
import com.prakhar.crm.repository.ContractRepository;
import com.prakhar.crm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContractRepository contractRepository;

    public DashboardResponse getStats() {
        long totalClients = clientRepository.count();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long totalContracts = contractRepository.count();
        long activeContracts = contractRepository.countByStatus(ContractStatus.ACTIVE);
        long draftContracts = contractRepository.countByStatus(ContractStatus.DRAFT);
        long expiredContracts = contractRepository.countByStatus(ContractStatus.EXPIRED);
        long cancelledContracts = contractRepository.countByStatus(ContractStatus.CANCELLED);

        Double allSum = contractRepository.sumAllContractValues();
        Double activeSum = contractRepository.sumContractValuesByStatus(ContractStatus.ACTIVE);

        double totalContractValue = allSum != null ? allSum : 0.0;
        double activeContractValue = activeSum != null ? activeSum : 0.0;

        return new DashboardResponse(
                totalClients,
                totalUsers,
                activeUsers,
                totalContracts,
                activeContracts,
                draftContracts,
                expiredContracts,
                cancelledContracts,
                totalContractValue,
                activeContractValue
        );
    }
}