package com.prakhar.crm.service;

import com.prakhar.crm.dto.DashboardResponse;
import com.prakhar.crm.entity.ContractStatus;
import com.prakhar.crm.repository.ClientRepository;
import com.prakhar.crm.repository.ContractRepository;
import com.prakhar.crm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getStats_shouldReturnAggregatedCountsAndValues() {
        when(clientRepository.count()).thenReturn(12L);
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByActiveTrue()).thenReturn(4L);
        when(contractRepository.count()).thenReturn(10L);
        when(contractRepository.countByStatus(ContractStatus.ACTIVE)).thenReturn(6L);
        when(contractRepository.countByStatus(ContractStatus.DRAFT)).thenReturn(2L);
        when(contractRepository.countByStatus(ContractStatus.EXPIRED)).thenReturn(1L);
        when(contractRepository.countByStatus(ContractStatus.CANCELLED)).thenReturn(1L);
        when(contractRepository.sumAllContractValues()).thenReturn(50000.0);
        when(contractRepository.sumContractValuesByStatus(ContractStatus.ACTIVE)).thenReturn(35000.0);

        DashboardResponse res = dashboardService.getStats();

        assertEquals(12L, res.getTotalClients());
        assertEquals(5L, res.getTotalUsers());
        assertEquals(4L, res.getActiveUsers());
        assertEquals(10L, res.getTotalContracts());
        assertEquals(6L, res.getActiveContracts());
        assertEquals(2L, res.getDraftContracts());
        assertEquals(1L, res.getExpiredContracts());
        assertEquals(1L, res.getCancelledContracts());
        assertEquals(50000.0, res.getTotalContractValue());
        assertEquals(35000.0, res.getActiveContractValue());
    }

    @Test
    void getStats_shouldDefaultToZero_whenSumQueriesReturnNull() {
        when(clientRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByActiveTrue()).thenReturn(0L);
        when(contractRepository.count()).thenReturn(0L);
        when(contractRepository.countByStatus(ContractStatus.ACTIVE)).thenReturn(0L);
        when(contractRepository.countByStatus(ContractStatus.DRAFT)).thenReturn(0L);
        when(contractRepository.countByStatus(ContractStatus.EXPIRED)).thenReturn(0L);
        when(contractRepository.countByStatus(ContractStatus.CANCELLED)).thenReturn(0L);
        when(contractRepository.sumAllContractValues()).thenReturn(null);
        when(contractRepository.sumContractValuesByStatus(ContractStatus.ACTIVE)).thenReturn(null);

        DashboardResponse res = dashboardService.getStats();

        assertEquals(0.0, res.getTotalContractValue());
        assertEquals(0.0, res.getActiveContractValue());
    }
}