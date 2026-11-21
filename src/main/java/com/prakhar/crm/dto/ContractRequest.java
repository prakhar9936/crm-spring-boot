package com.prakhar.crm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prakhar.crm.entity.ContractStatus;
import jakarta.validation.constraints.AssertTrue;
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

    @NotNull(message = "Status is required")
    private ContractStatus status;

    @JsonIgnore
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}