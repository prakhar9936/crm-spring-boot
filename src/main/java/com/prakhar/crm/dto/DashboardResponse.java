package com.prakhar.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private long totalClients;
    private long totalUsers;
    private long activeUsers;
    private long totalContracts;
    private long activeContracts;
    private long draftContracts;
    private long expiredContracts;
    private long cancelledContracts;
    private double totalContractValue;
    private double activeContractValue;
}