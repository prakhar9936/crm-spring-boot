package com.prakhar.crm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prakhar.crm.repository.ClientRepository;
import com.prakhar.crm.repository.ContractRepository;
import com.prakhar.crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CrmIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        contractRepository.deleteAll();
        clientRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String registerAndLogin(String firstName, String lastName, String email, String password) throws Exception {
        String registerJson = String.format(
                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                firstName, lastName, email, password
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk());

        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    @Test
    void unauthenticatedAccess_isBlocked() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isForbidden());
    }

    @Test
    void firstUserIsAdmin_secondIsEmployee_andAdminRoutesEnforceRole() throws Exception {
        String adminToken = registerAndLogin("Admin", "User", "admin@crm.com", "password123");
        String empToken = registerAndLogin("Emp", "User", "emp@crm.com", "password123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void endToEndClientContractAndDashboardFlow() throws Exception {
        String adminToken = registerAndLogin("Admin", "User", "admin@crm.com", "password123");

        String clientJson = "{\"firstName\":\"Acme\",\"lastName\":\"Corp\",\"email\":\"contact@acme.com\",\"phone\":\"9876543210\",\"companyName\":\"Acme Ltd\"}";
        MvcResult clientResult = mockMvc.perform(post("/api/clients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("contact@acme.com"))
                .andReturn();

        long clientId = objectMapper.readTree(clientResult.getResponse().getContentAsString()).get("id").asLong();

        String contractJson = "{\"title\":\"Annual Support\",\"value\":50000.0,\"status\":\"ACTIVE\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-12-31\"}";
        MvcResult contractResult = mockMvc.perform(post("/api/clients/" + clientId + "/contracts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contractJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Annual Support"))
                .andReturn();

        long contractId = objectMapper.readTree(contractResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/clients/" + clientId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClients").value(1))
                .andExpect(jsonPath("$.totalContracts").value(1));

        mockMvc.perform(delete("/api/contracts/" + contractId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/clients/" + clientId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void validationAndDeactivationFlow() throws Exception {
        String adminToken = registerAndLogin("Admin", "User", "admin@crm.com", "password123");
        registerAndLogin("Emp", "User", "emp@crm.com", "password123");

        mockMvc.perform(post("/api/clients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());

        MvcResult usersResult = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = objectMapper.readTree(usersResult.getResponse().getContentAsString()).get("content");
        long empId = content.get(0).get("email").asText().equals("emp@crm.com")
                ? content.get(0).get("id").asLong()
                : content.get(1).get("id").asLong();

        mockMvc.perform(put("/api/users/" + empId + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"emp@crm.com\",\"password\":\"password123\"}"))
                .andExpect(status().isForbidden());
    }
}