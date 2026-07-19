package com.prakhar.crm.dto;

import com.prakhar.crm.entity.ContractStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContractRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Value is required")
    private Double value;

    private LocalDate startDate;

    private LocalDate endDate;

    private ContractStatus status;
}