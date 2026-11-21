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
        return ResponseEntity.ok(clientService.createClient(request, loggedInEmail));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ClientResponse>> getMyClients(Authentication authentication) {
        String loggedInEmail = authentication.getName();
        return ResponseEntity.ok(clientService.getMyClients(loggedInEmail));
    }

    @GetMapping
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clientService.getAllClients(page, size));
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
            @Valid @RequestBody ClientRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(clientService.updateClient(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(
            @PathVariable Long id,
            Authentication authentication) {
        clientService.deleteClient(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}