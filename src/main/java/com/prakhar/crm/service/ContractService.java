package com.prakhar.crm.service;

import com.prakhar.crm.dto.ContractRequest;
import com.prakhar.crm.dto.ContractResponse;
import com.prakhar.crm.entity.Client;
import com.prakhar.crm.entity.Contract;
import com.prakhar.crm.exception.ResourceNotFoundException;
import com.prakhar.crm.repository.ClientRepository;
import com.prakhar.crm.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ClientRepository clientRepository;

    public ContractResponse createContract(Long clientId, ContractRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        Contract contract = new Contract();
        contract.setTitle(request.getTitle());
        contract.setValue(request.getValue());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setStatus(request.getStatus());
        contract.setClient(client);

        Contract saved = contractRepository.save(contract);
        return toResponse(saved);
    }

    public List<ContractResponse> getContractsByClient(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }

        return contractRepository.findByClientId(clientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ContractResponse updateContract(Long contractId, ContractRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + contractId));

        contract.setTitle(request.getTitle());
        contract.setValue(request.getValue());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setStatus(request.getStatus());

        Contract updated = contractRepository.save(contract);
        return toResponse(updated);
    }

    public void deleteContract(Long contractId) {
        if (!contractRepository.existsById(contractId)) {
            throw new ResourceNotFoundException("Contract not found with id: " + contractId);
        }
        contractRepository.deleteById(contractId);
    }

    private ContractResponse toResponse(Contract contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getTitle(),
                contract.getValue(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getStatus(),
                contract.getClient().getId(),
                contract.getClient().getFirstName() + " " + contract.getClient().getLastName()
        );
    }
}