package com.prakhar.crm.service;

import com.prakhar.crm.dto.ContractRequest;
import com.prakhar.crm.dto.ContractResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.prakhar.crm.entity.Client;
import com.prakhar.crm.entity.Contract;
import com.prakhar.crm.entity.ContractStatus;
import com.prakhar.crm.entity.Role;
import com.prakhar.crm.entity.User;
import com.prakhar.crm.exception.ForbiddenException;
import com.prakhar.crm.repository.ClientRepository;
import com.prakhar.crm.repository.ContractRepository;
import com.prakhar.crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContractService contractService;

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

    private Contract makeContract(Long id, Client client) {
        Contract c = new Contract();
        c.setId(id);
        c.setTitle("Old title");
        c.setValue(1000.0);
        c.setStatus(ContractStatus.DRAFT);
        c.setClient(client);
        return c;
    }

    private ContractRequest makeRequest() {
        ContractRequest r = new ContractRequest();
        r.setTitle("New title");
        r.setValue(2000.0);
        r.setStartDate(LocalDate.of(2026, 1, 1));
        r.setEndDate(LocalDate.of(2026, 12, 31));
        r.setStatus(ContractStatus.ACTIVE);
        return r;
    }

    @Test
    void createContract_shouldThrowForbidden_whenEmployeeIsNotOwner() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        User other = makeUser("other@x.com", Role.EMPLOYEE);
        when(userRepository.findByEmail("other@x.com")).thenReturn(Optional.of(other));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(makeClient(1L, owner)));

        assertThrows(ForbiddenException.class,
                () -> contractService.createContract(1L, makeRequest(), "other@x.com"));

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void updateContract_shouldThrowForbidden_whenEmployeeIsNotOwner() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        User other = makeUser("other@x.com", Role.EMPLOYEE);
        when(userRepository.findByEmail("other@x.com")).thenReturn(Optional.of(other));
        when(contractRepository.findById(10L))
                .thenReturn(Optional.of(makeContract(10L, makeClient(1L, owner))));

        assertThrows(ForbiddenException.class,
                () -> contractService.updateContract(10L, makeRequest(), "other@x.com"));

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void deleteContract_shouldThrowForbidden_whenEmployeeIsNotOwner() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        User other = makeUser("other@x.com", Role.EMPLOYEE);
        when(userRepository.findByEmail("other@x.com")).thenReturn(Optional.of(other));
        when(contractRepository.findById(10L))
                .thenReturn(Optional.of(makeContract(10L, makeClient(1L, owner))));

        assertThrows(ForbiddenException.class,
                () -> contractService.deleteContract(10L, "other@x.com"));

        verify(contractRepository, never()).deleteById(10L);
    }

    @Test
    void updateContract_shouldWork_whenEmployeeIsOwner() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        when(userRepository.findByEmail("owner@x.com")).thenReturn(Optional.of(owner));
        when(contractRepository.findById(10L))
                .thenReturn(Optional.of(makeContract(10L, makeClient(1L, owner))));
        when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

        contractService.updateContract(10L, makeRequest(), "owner@x.com");

        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    void deleteContract_shouldWork_whenAdminIsNotOwner() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        User admin = makeUser("admin@x.com", Role.ADMIN);
        when(userRepository.findByEmail("admin@x.com")).thenReturn(Optional.of(admin));
        when(contractRepository.findById(10L))
                .thenReturn(Optional.of(makeContract(10L, makeClient(1L, owner))));

        contractService.deleteContract(10L, "admin@x.com");

        verify(contractRepository, times(1)).deleteById(10L);
    }

    @Test
    void getContracts_shouldReturnAll_whenStatusIsNull() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        Contract c = makeContract(10L, makeClient(1L, owner));
        Pageable pageable = PageRequest.of(0, 10);
        when(contractRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(c)));

        Page<ContractResponse> result = contractService.getContracts(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Old title", result.getContent().get(0).getTitle());
        verify(contractRepository, times(1)).findAll(pageable);
        verify(contractRepository, never()).findByStatus(any(), any());
    }

    @Test
    void getContracts_shouldFilterByStatus_whenStatusIsProvided() {
        User owner = makeUser("owner@x.com", Role.EMPLOYEE);
        Contract c = makeContract(10L, makeClient(1L, owner));
        c.setStatus(ContractStatus.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10);
        when(contractRepository.findByStatus(ContractStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(c)));

        Page<ContractResponse> result = contractService.getContracts(ContractStatus.ACTIVE, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(ContractStatus.ACTIVE, result.getContent().get(0).getStatus());
        verify(contractRepository, times(1)).findByStatus(ContractStatus.ACTIVE, pageable);
        verify(contractRepository, never()).findAll(any(Pageable.class));
    }}