package com.femcoders.tico.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.tico.dto.request.LabelRequest;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.UserRepository;
import com.femcoders.tico.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class LabelCreationFlowIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createLabel_asAdmin_withValidData_returns201AndLabelSavedInDB() throws Exception {
        User admin = saveUser("admin@test.com", UserRole.ADMIN);
        String token = jwtTokenService.createToken(
                admin.getEmail(), admin.getId(), Set.of("ROLE_ADMIN"));

        LabelRequest dto = new LabelRequest("Urgente", "#FF5733");

        mockMvc.perform(post("/api/labels")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Urgente"))
                .andExpect(jsonPath("$.color").value("#FF5733"))
                .andExpect(jsonPath("$.active").value(true));

        Optional<Label> saved = labelRepository.findByNameContainingIgnoreCase("Urgente").stream().findFirst();
        assertTrue(saved.isPresent());
        assertEquals("#FF5733", saved.get().getColor());
        assertTrue(saved.get().getIsActive());
    }

    @Test
    void createLabel_asEmployee_returns403() throws Exception {
        User employee = saveUser("employee@test.com", UserRole.EMPLOYEE);
        String token = jwtTokenService.createToken(
                employee.getEmail(), employee.getId(), Set.of("ROLE_EMPLOYEE"));

        LabelRequest dto = new LabelRequest("Bloqueado", "#AABBCC");

        mockMvc.perform(post("/api/labels")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createLabel_withoutAuthentication_returns401() throws Exception {
        LabelRequest dto = new LabelRequest("Sin auth", "#123456");

        mockMvc.perform(post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createLabel_withInvalidColorFormat_returns400() throws Exception {
        User admin = saveUser("admin2@test.com", UserRole.ADMIN);
        String token = jwtTokenService.createToken(
                admin.getEmail(), admin.getId(), Set.of("ROLE_ADMIN"));

        LabelRequest dto = new LabelRequest("Color malo", "rojo");

        mockMvc.perform(post("/api/labels")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLabel_withDuplicateName_returns500() throws Exception {
        User admin = saveUser("admin3@test.com", UserRole.ADMIN);
        String token = jwtTokenService.createToken(
                admin.getEmail(), admin.getId(), Set.of("ROLE_ADMIN"));

        Label existing = new Label();
        existing.setName("Duplicado");
        existing.setColor("#000000");
        labelRepository.save(existing);

        LabelRequest dto = new LabelRequest("Duplicado", "#111111");

        mockMvc.perform(post("/api/labels")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is5xxServerError());
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
