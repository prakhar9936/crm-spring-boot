package com.prakhar.crm.controller;

import com.prakhar.crm.dto.ClientRequest;
import com.prakhar.crm.dto.ClientResponse;
import com.prakhar.crm.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }
}