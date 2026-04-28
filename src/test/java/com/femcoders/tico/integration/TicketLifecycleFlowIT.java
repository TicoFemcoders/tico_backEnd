package com.femcoders.tico.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.tico.dto.request.AssignAdminRequest;
import com.femcoders.tico.dto.request.CloseTicketRequest;
import com.femcoders.tico.dto.request.TicketCreateRequest;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.enums.UserRole;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TicketLifecycleFlowIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TicketRepository ticketRepository;
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
    void fullLifecycle_createAssignCloseReopen() throws Exception {
        // 1. Employee creates ticket → OPEN
        TicketCreateRequest createDto = new TicketCreateRequest(
                "El correo corporativo no funciona",
                "Desde esta mañana no puedo acceder al correo de empresa.",
                TicketPriority.HIGH,
                List.of());

        MvcResult createResult = mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.emailSubject").value(
                        org.hamcrest.Matchers.startsWith("[TICO-")))
                .andReturn();

        Long ticketId = objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asLong();

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        assertNull(ticket.getAssignedTo());

        // 2. Admin assigns themselves → status changes automatically to IN_PROGRESS
        AssignAdminRequest assignDto = new AssignAdminRequest(admin.getId());

        mockMvc.perform(patch("/api/tickets/{id}/assign-admin", ticketId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedToName").value("Test User"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        ticketRepository.flush();
        ticket = ticketRepository.findById(ticketId).orElseThrow();
        assertEquals(admin.getId(), ticket.getAssignedTo().getId());
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());

        // 3. Admin closes ticket with a message
        CloseTicketRequest closeDto = new CloseTicketRequest("Problema resuelto: cuenta restablecida.");

        mockMvc.perform(patch("/api/tickets/{id}/close", ticketId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        ticketRepository.flush();
        ticket = ticketRepository.findById(ticketId).orElseThrow();
        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        assertNotNull(ticket.getClosedAt());
        assertEquals("Problema resuelto: cuenta restablecida.", ticket.getClosingMessage());

        // 4. Employee reopens the ticket → back to IN_PROGRESS
        mockMvc.perform(patch("/api/tickets/{id}/reopen", ticketId)
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        ticketRepository.flush();
        ticket = ticketRepository.findById(ticketId).orElseThrow();
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        assertNull(ticket.getClosedAt());
    }

    @Test
    void assignAdmin_asEmployee_returns403() throws Exception {
        Ticket ticket = createTicketDirectly();
        AssignAdminRequest dto = new AssignAdminRequest(admin.getId());

        mockMvc.perform(patch("/api/tickets/{id}/assign-admin", ticket.getId())
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void closeTicket_asEmployee_returns403() throws Exception {
        Ticket ticket = createTicketDirectly();
        CloseTicketRequest dto = new CloseTicketRequest("Intento de cierre no autorizado");

        mockMvc.perform(patch("/api/tickets/{id}/close", ticket.getId())
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    private Ticket createTicketDirectly() {
        Ticket ticket = new Ticket();
        ticket.setTitle("Ticket de prueba directo");
        ticket.setDescription("Descripción de prueba suficientemente larga.");
        ticket.setPriority(TicketPriority.LOW);
        ticket.setCreatedBy(employee);
        return ticketRepository.save(ticket);
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
