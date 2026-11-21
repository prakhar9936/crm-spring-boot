package com.prakhar.crm.service;

import com.prakhar.crm.dto.ClientRequest;
import com.prakhar.crm.entity.Client;
import com.prakhar.crm.entity.Role;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.ConflictException;
import com.prakhar.crm.exception.ForbiddenException;
import com.prakhar.crm.repository.ClientRepository;
import com.prakhar.crm.repository.ContractRepository;
import com.prakhar.crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User makeUser(String email, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setRole(role);
        return u;
    }

    private Client makeClient(Long id, User owner) {
        Client c = new Client();
        c.setId(id);
        c.setFirstName("Raj");
        c.setLastName("Roy");
        c.setEmail("raj@demo.com");
        c.setAssignedUser(owner);
        return c;
    }

    private ClientRequest makeRequest() {
        ClientRequest r = new ClientRequest();
        r.setFirstName("Raj");
        r.setLastName("Roy");
        r.setEmail("raj@demo.com");
        r.setPhone("7777777777");
        r.setCompanyName("Demo Ltd");
        return r;
    }

    @Test
    void deleteClient_shouldThrowConflict_whenClientHasContracts() {
        User admin = makeUser("admin@x.com", Role.ADMIN);
        when(userRepository.findByEmail("admin@x.com")).thenReturn(Optional.of(admin));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(makeClient(1L, admin)));
        when(contractRepository.existsByClientId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> clientService.deleteClient(1L, "admin@x.com"));

        verify(clientRepository, never()).deleteById(1L);
    }

    @Test
    void deleteClient_shouldDelete_whenClientHasNoContracts() {
        User admin = makeUser("admin@x.com", Role.ADMIN);
        when(userRepository.findByEmail("admin@x.com")).thenReturn(Optional.of(admin));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(makeClient(1L, admin)));
        when(contractRepository.existsByClientId(1L)).thenReturn(false);

        clientService.deleteClient(1L, "admin@x.com");

        verify(clientRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateClient_shouldThrowForbidden_whenEmployeeIsNotOwner() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        User other = makeUser("other@x.com", Role.EMPLOYEE);
        when(userRepository.findByEmail("other@x.com")).thenReturn(Optional.of(other));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(makeClient(1L, owner)));

        assertThrows(ForbiddenException.class,
                () -> clientService.updateClient(1L, makeRequest(), "other@x.com"));

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClient_shouldWork_whenEmployeeIsOwner() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        when(userRepository.findByEmail("owner@x.com")).thenReturn(Optional.of(owner));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(makeClient(1L, owner)));
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        clientService.updateClient(1L, makeRequest(), "owner@x.com");

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClient_shouldWork_whenAdminIsNotOwner() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        User admin = makeUser("admin@x.com", Role.ADMIN);
        when(userRepository.findByEmail("admin@x.com")).thenReturn(Optional.of(admin));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(makeClient(1L, owner)));
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        clientService.updateClient(1L, makeRequest(), "admin@x.com");

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void deleteClient_shouldThrowForbidden_whenEmployeeIsNotOwner() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        User other = makeUser("other@x.com", Role.EMPLOYEE);
        when(userRepository.findByEmail("other@x.com")).thenReturn(Optional.of(other));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(makeClient(1L, owner)));

        assertThrows(ForbiddenException.class, () -> clientService.deleteClient(1L, "other@x.com"));

        verify(clientRepository, never()).deleteById(1L);
    }
}