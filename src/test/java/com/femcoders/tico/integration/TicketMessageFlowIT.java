package com.femcoders.tico.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.tico.dto.request.AssignAdminRequest;
import com.femcoders.tico.dto.request.TicketCreateRequest;
import com.femcoders.tico.dto.request.TicketMessageRequest;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.repository.TicketMessageRepository;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.repository.UserRepository;
import com.femcoders.tico.security.JwtTokenService;
import com.femcoders.tico.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TicketMessageFlowIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketMessageRepository ticketMessageRepository;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    private User employee;
    private User admin;
    private String employeeToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        employee = saveUser("employee@test.com", UserRole.EMPLOYEE);
        admin = saveUser("admin@test.com", UserRole.ADMIN);
        employeeToken = jwtTokenService.createToken(
                employee.getEmail(), employee.getId(), Set.of("ROLE_EMPLOYEE"));
        adminToken = jwtTokenService.createToken(
                admin.getEmail(), admin.getId(), Set.of("ROLE_ADMIN"));
    }

    @Test
    void assignedAdmin_canSendMessage_andEmployeeCanSendReply() throws Exception {
        Long ticketId = createTicketAndAssignAdmin();

        // Admin replies to the ticket
        TicketMessageRequest adminMsg = new TicketMessageRequest(ticketId, "Estamos investigando el problema.", null);

        mockMvc.perform(post("/api/tickets/{id}/messages", ticketId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminMsg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Estamos investigando el problema."))
                .andExpect(jsonPath("$.authorName").value("Test User"));

        // Employee replies back
        TicketMessageRequest employeeMsg = new TicketMessageRequest(ticketId, "Gracias, quedo a la espera.", null);

        mockMvc.perform(post("/api/tickets/{id}/messages", ticketId)
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeMsg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Gracias, quedo a la espera."));

        ticketMessageRepository.flush();
        long messageCount = ticketMessageRepository
                .findByTicketIdAndRecipientIdIsNullOrderByCreatedAtDesc(ticketId,
                        org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();
        assertEquals(2, messageCount);
    }

    @Test
    void employee_cannotSendMessage_onAnotherEmployeesTicket() throws Exception {
        Long ticketId = createTicketAndAssignAdmin();

        User otherEmployee = saveUser("other@test.com", UserRole.EMPLOYEE);
        String otherToken = jwtTokenService.createToken(
                otherEmployee.getEmail(), otherEmployee.getId(), Set.of("ROLE_EMPLOYEE"));

        TicketMessageRequest msg = new TicketMessageRequest(ticketId, "Intentando escribir en ticket ajeno.", null);

        mockMvc.perform(post("/api/tickets/{id}/messages", ticketId)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unassignedAdmin_cannotSendMessage_onTicketAssignedToOther() throws Exception {
        Long ticketId = createTicketAndAssignAdmin();

        User otherAdmin = saveUser("other.admin@test.com", UserRole.ADMIN);
        String otherAdminToken = jwtTokenService.createToken(
                otherAdmin.getEmail(), otherAdmin.getId(), Set.of("ROLE_ADMIN"));

        TicketMessageRequest msg = new TicketMessageRequest(ticketId, "Admin no asignado intentando escribir.", null);

        mockMvc.perform(post("/api/tickets/{id}/messages", ticketId)
                .header("Authorization", "Bearer " + otherAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendMessage_withoutAuthentication_returns401() throws Exception {
        Long ticketId = createTicketAndAssignAdmin();

        TicketMessageRequest msg = new TicketMessageRequest(ticketId, "Sin autenticación.", null);

        mockMvc.perform(post("/api/tickets/{id}/messages", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isUnauthorized());
    }

    // Creates a ticket as employee and assigns it to admin via the API
    private Long createTicketAndAssignAdmin() throws Exception {
        TicketCreateRequest createDto = new TicketCreateRequest(
                "Problema con la VPN corporativa",
                "No puedo conectarme a la VPN desde casa desde ayer.",
                TicketPriority.HIGH,
                List.of());

        MvcResult result = mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long ticketId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        AssignAdminRequest assignDto = new AssignAdminRequest(admin.getId());
        mockMvc.perform(patch("/api/tickets/{id}/assign-admin", ticketId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignDto)))
                .andExpect(status().isOk());

        return ticketId;
    }

    private User saveUser(String email, UserRole role) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash("hashed_temp");
        user.setIsActive(true);
        user.getRoles().add(role);
        return userRepository.save(user);
    }
}
