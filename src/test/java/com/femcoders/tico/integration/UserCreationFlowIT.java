package com.femcoders.tico.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.tico.dto.request.AdminCreateUserRequest;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.repository.ActivationTokenRepository;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserCreationFlowIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ActivationTokenRepository tokenRepository;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @Test
    void createUser_asAdmin_withValidData_returns201AndUserSavedInactive() throws Exception {
        User admin = saveUser("admin@test.com", UserRole.ADMIN);
        String token = jwtTokenService.createToken(
                admin.getEmail(), admin.getId(), Set.of("ROLE_ADMIN"));

        AdminCreateUserRequest dto = new AdminCreateUserRequest(
                "Ana García", "ana.garcia@empresa.com", Set.of(UserRole.EMPLOYEE));

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ana García"))
                .andExpect(jsonPath("$.email").value("ana.garcia@empresa.com"))
                .andExpect(jsonPath("$.isActive").value(false));

        User saved = userRepository.findByEmail("ana.garcia@empresa.com").orElseThrow();
        assertFalse(saved.getIsActive());
        assertTrue(saved.getRoles().contains(UserRole.EMPLOYEE));

        boolean hasActivationToken = tokenRepository
                .findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
                        "ana.garcia@empresa.com", TokenType.ACTIVATION)
                .isPresent();
        assertTrue(hasActivationToken);
    }

    @Test
    void createUser_asEmployee_returns403() throws Exception {
        User employee = saveUser("employee@test.com", UserRole.EMPLOYEE);
        String token = jwtTokenService.createToken(
                employee.getEmail(), employee.getId(), Set.of("ROLE_EMPLOYEE"));

        AdminCreateUserRequest dto = new AdminCreateUserRequest(
                "Carlos López", "carlos@empresa.com", Set.of(UserRole.EMPLOYEE));

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_withoutAuthentication_returns401() throws Exception {
        AdminCreateUserRequest dto = new AdminCreateUserRequest(
                "Sin Auth", "sinauth@empresa.com", Set.of(UserRole.EMPLOYEE));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUser_withInvalidEmail_returns400() throws Exception {
        User admin = saveUser("admin2@test.com", UserRole.ADMIN);
        String token = jwtTokenService.createToken(
                admin.getEmail(), admin.getId(), Set.of("ROLE_ADMIN"));

        AdminCreateUserRequest dto = new AdminCreateUserRequest(
                "Pedro Ruiz", "no-es-un-email", Set.of(UserRole.EMPLOYEE));

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withBlankName_returns400() throws Exception {
        User admin = saveUser("admin3@test.com", UserRole.ADMIN);
        String token = jwtTokenService.createToken(
                admin.getEmail(), admin.getId(), Set.of("ROLE_ADMIN"));

        AdminCreateUserRequest dto = new AdminCreateUserRequest(
                "", "valido@empresa.com", Set.of(UserRole.EMPLOYEE));

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withEmptyRoles_returns400() throws Exception {
        User admin = saveUser("admin4@test.com", UserRole.ADMIN);
        String token = jwtTokenService.createToken(
                admin.getEmail(), admin.getId(), Set.of("ROLE_ADMIN"));

        AdminCreateUserRequest dto = new AdminCreateUserRequest(
                "María Sánchez", "maria@empresa.com", Set.of());

        mockMvc.perform(post("/api/users")
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
