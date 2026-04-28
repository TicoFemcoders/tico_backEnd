package com.femcoders.tico.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TicketCreationFlowIT {

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

    @Test
    void createTicket_withValidData_returns201AndTicketSavedInDB() throws Exception {
        User employee = saveUser("elena@test.com", UserRole.EMPLOYEE);
        String token = jwtTokenService.createToken(
                employee.getEmail(), employee.getId(), Set.of("ROLE_EMPLOYEE"));

        TicketCreateRequest dto = new TicketCreateRequest(
                "Mi impresora no funciona",
                "Desde esta mañana la impresora de la planta 2 no imprime absolutamente nada.",
                TicketPriority.MEDIUM,
                List.of());

        mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Mi impresora no funciona"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.emailSubject").value(startsWith("[TICO-")));

        List<Ticket> tickets = ticketRepository.findByCreatedById(employee.getId());
        assertEquals(1, tickets.size());
        Ticket saved = tickets.get(0);
        assertEquals("Mi impresora no funciona", saved.getTitle());
        assertEquals(TicketStatus.OPEN, saved.getStatus());
        assertEquals(TicketPriority.MEDIUM, saved.getPriority());
        assertNotNull(saved.getEmailSubject());
        assertTrue(saved.getEmailSubject().startsWith("[TICO-"));
    }

    @Test
    void createTicket_withoutAuthentication_returns401() throws Exception {
        TicketCreateRequest dto = new TicketCreateRequest(
                "Ticket sin autenticación",
                "Este ticket no debería crearse porque no hay token JWT.",
                TicketPriority.LOW,
                List.of());

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTicket_withBlankTitle_returns400() throws Exception {
        User employee = saveUser("mario@test.com", UserRole.EMPLOYEE);
        String token = jwtTokenService.createToken(
                employee.getEmail(), employee.getId(), Set.of("ROLE_EMPLOYEE"));

        TicketCreateRequest dto = new TicketCreateRequest(
                "",
                "Descripción suficientemente larga para superar el mínimo de validación.",
                TicketPriority.HIGH,
                List.of());

        mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTicket_withTitleTooShort_returns400() throws Exception {
        User employee = saveUser("lucia@test.com", UserRole.EMPLOYEE);
        String token = jwtTokenService.createToken(
                employee.getEmail(), employee.getId(), Set.of("ROLE_EMPLOYEE"));

        TicketCreateRequest dto = new TicketCreateRequest(
                "Bug",
                "Descripción suficientemente larga para superar el mínimo de validación.",
                TicketPriority.LOW,
                List.of());

        mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
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
