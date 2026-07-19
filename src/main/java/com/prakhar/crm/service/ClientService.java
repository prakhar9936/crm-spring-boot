package com.prakhar.crm.service;

import com.prakhar.crm.dto.ClientRequest;
import com.prakhar.crm.dto.ClientResponse;
import com.prakhar.crm.entity.Client;
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
    Page<Client> clients = clientRepository.findByFirstNameContainingIgnoreCase(name, pageable);
    return clients.map(this::toResponse);
    }

    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return toResponse(client);
    }

    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setCompanyName(request.getCompanyName());

        Client updated = clientRepository.save(client);
        return toResponse(updated);
    }

    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }

    public Page<ClientResponse> getAllClients(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return clientRepository.findAll(pageable)
            .map(this::toResponse);
}

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getCompanyName(),
                client.getAssignedUser() != null ? client.getAssignedUser().getEmail() : null
        );
    }
}