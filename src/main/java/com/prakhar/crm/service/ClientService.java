package com.prakhar.crm.service;

import com.prakhar.crm.exception.ConflictException;
import com.prakhar.crm.exception.ForbiddenException;
import com.prakhar.crm.repository.ContractRepository;
import com.prakhar.crm.dto.ClientRequest;
import com.prakhar.crm.dto.ClientResponse;
import com.prakhar.crm.entity.Client;
import com.prakhar.crm.entity.Role;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.EmailAlreadyExistsException;
import com.prakhar.crm.exception.ResourceNotFoundException;
import com.prakhar.crm.repository.ClientRepository;
import com.prakhar.crm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContractRepository contractRepository;

    public ClientResponse createClient(ClientRequest request, String loggedInEmail) {

        if (request.getEmail() != null && clientRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Client email already exists: " + request.getEmail());
        }

        User currentUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setCompanyName(request.getCompanyName());
        client.setAssignedUser(currentUser);

        Client saved = clientRepository.save(client);
        return toResponse(saved);
    }

    public List<ClientResponse> getMyClients(String loggedInEmail) {
        User currentUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        return clientRepository.findByAssignedUser(currentUser)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<ClientResponse> searchClients(String name, Pageable pageable) {

        if (name == null || name.isBlank()) {
            return clientRepository.findAll(pageable)
                    .map(this::toResponse);
        }

        String searchName = name.trim();

        Page<Client> clients = clientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchName,
                searchName,
                pageable);

        return clients.map(this::toResponse);
    }

    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return toResponse(client);
    }

    public ClientResponse updateClient(Long id, ClientRequest request, String loggedInEmail) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        checkOwnerOrAdmin(client, loggedInEmail);

        if (request.getEmail() != null
                && !request.getEmail().equalsIgnoreCase(client.getEmail())
                && clientRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Client email already exists: " + request.getEmail());
        }

        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setCompanyName(request.getCompanyName());

        Client updated = clientRepository.save(client);
        return toResponse(updated);
    }

    public void deleteClient(Long id, String loggedInEmail) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        checkOwnerOrAdmin(client, loggedInEmail);

        if (contractRepository.existsByClientId(id)) {
            throw new ConflictException("Cannot delete client " + id
                    + " because it still has contracts. Delete the contracts first.");
        }
        clientRepository.deleteById(id);
    }

    public Page<ClientResponse> getAllClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clientRepository.findAll(pageable)
                .map(this::toResponse);
    }

    private void checkOwnerOrAdmin(Client client, String loggedInEmail) {
        User user = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (client.getAssignedUser() == null
                || !loggedInEmail.equalsIgnoreCase(client.getAssignedUser().getEmail())) {
            throw new ForbiddenException("You can only change your own clients");
        }
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getCompanyName(),
                client.getAssignedUser() != null ? client.getAssignedUser().getEmail() : null);
    }
}