package com.prakhar.crm.service;

import com.prakhar.crm.dto.ContractRequest;
import com.prakhar.crm.dto.ContractResponse;
import com.prakhar.crm.entity.Client;
import com.prakhar.crm.entity.Contract;
import com.prakhar.crm.entity.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.prakhar.crm.entity.Role;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.ForbiddenException;
import com.prakhar.crm.exception.ResourceNotFoundException;
import com.prakhar.crm.repository.ClientRepository;
import com.prakhar.crm.repository.ContractRepository;
import com.prakhar.crm.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    public ContractResponse createContract(Long clientId, ContractRequest request, String loggedInEmail) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        checkOwnerOrAdmin(client, loggedInEmail);

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

    public ContractResponse updateContract(Long contractId, ContractRequest request, String loggedInEmail) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + contractId));

        checkOwnerOrAdmin(contract.getClient(), loggedInEmail);

        contract.setTitle(request.getTitle());
        contract.setValue(request.getValue());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setStatus(request.getStatus());

        Contract updated = contractRepository.save(contract);
        return toResponse(updated);
    }

    public void deleteContract(Long contractId, String loggedInEmail) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + contractId));

        checkOwnerOrAdmin(contract.getClient(), loggedInEmail);

        contractRepository.deleteById(contractId);
    }

    private void checkOwnerOrAdmin(Client client, String loggedInEmail) {
        User user = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (client.getAssignedUser() == null
                || !loggedInEmail.equalsIgnoreCase(client.getAssignedUser().getEmail())) {
            throw new ForbiddenException("You can only change contracts of your own clients");
        }
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

    public Page<ContractResponse> getContracts(ContractStatus status, Pageable pageable) {
        if (status != null) {
            return contractRepository.findByStatus(status, pageable).map(this::toResponse);
        }
        return contractRepository.findAll(pageable).map(this::toResponse);
    }
}