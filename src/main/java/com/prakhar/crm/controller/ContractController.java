package com.prakhar.crm.controller;

import com.prakhar.crm.dto.ContractRequest;
import com.prakhar.crm.dto.ContractResponse;
import com.prakhar.crm.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ContractController {

    @Autowired
    private ContractService contractService;

    @PostMapping("/api/clients/{clientId}/contracts")
    public ResponseEntity<ContractResponse> createContract(
            @PathVariable Long clientId,
            @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.ok(contractService.createContract(clientId, request));
    }

    @GetMapping("/api/clients/{clientId}/contracts")
    public ResponseEntity<List<ContractResponse>> getContractsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(contractService.getContractsByClient(clientId));
    }

    @PutMapping("/api/contracts/{id}")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.ok(contractService.updateContract(id, request));
    }

    @DeleteMapping("/api/contracts/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }
}