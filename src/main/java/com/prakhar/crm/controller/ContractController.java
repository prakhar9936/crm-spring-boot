package com.prakhar.crm.controller;

import com.prakhar.crm.dto.ContractRequest;
import com.prakhar.crm.dto.ContractResponse;
import com.prakhar.crm.entity.ContractStatus;
import com.prakhar.crm.service.ContractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ContractController {

    @Autowired
    private ContractService contractService;

    @PostMapping("/api/clients/{clientId}/contracts")
    public ResponseEntity<ContractResponse> createContract(
            @PathVariable Long clientId,
            @Valid @RequestBody ContractRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(contractService.createContract(clientId, request, authentication.getName()));
    }

    @GetMapping("/api/clients/{clientId}/contracts")
    public ResponseEntity<List<ContractResponse>> getContractsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(contractService.getContractsByClient(clientId));
    }

    @PutMapping("/api/contracts/{id}")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(contractService.updateContract(id, request, authentication.getName()));
    }

    @DeleteMapping("/api/contracts/{id}")
    public ResponseEntity<Void> deleteContract(
            @PathVariable Long id,
            Authentication authentication) {
        contractService.deleteContract(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/contracts")
    public ResponseEntity<Page<ContractResponse>> getContracts(
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(contractService.getContracts(status, pageable));
    }
}