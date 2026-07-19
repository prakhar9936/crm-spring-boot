package com.prakhar.crm.dto;

import com.prakhar.crm.entity.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ContractResponse {
    private Long id;
    private String title;
    private Double value;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;
    private Long clientId;
    private String clientName;
}