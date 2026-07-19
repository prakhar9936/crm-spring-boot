package com.prakhar.crm.controller;

import com.prakhar.crm.dto.ClientRequest;
import com.prakhar.crm.dto.ClientResponse;
import com.prakhar.crm.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody ClientRequest request,
            Authentication authentication) {

        String loggedInEmail = authentication.getName();
        ClientResponse response = clientService.createClient(request, loggedInEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ClientResponse>> getMyClients(Authentication authentication) {
        String loggedInEmail = authentication.getName();
        List<ClientResponse> clients = clientService.getMyClients(loggedInEmail);
        return ResponseEntity.ok(clients);
    }

@GetMapping
public ResponseEntity<Page<ClientResponse>> getAllClients(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    Page<ClientResponse> clients = clientService.getAllClients(page, size);
    return ResponseEntity.ok(clients);
}
@GetMapping("/search")
public ResponseEntity<Page<ClientResponse>> searchClients(
        @RequestParam(defaultValue = "") String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(clientService.searchClients(name, pageable));
}
    @GetMapping("/{id}")
public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
    return ResponseEntity.ok(clientService.getClientById(id));
}

@PutMapping("/{id}")
public ResponseEntity<ClientResponse> updateClient(
        @PathVariable Long id,
        @Valid @RequestBody ClientRequest request) {
    return ResponseEntity.ok(clientService.updateClient(id, request));
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
    clientService.deleteClient(id);
    return ResponseEntity.noContent().build();
}
}