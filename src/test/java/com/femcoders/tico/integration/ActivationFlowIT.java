package com.femcoders.tico.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.tico.dto.request.ResetPasswordConfirmRequest;
import com.femcoders.tico.dto.request.ActivationRequest;
import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.repository.ActivationTokenRepository;
import com.femcoders.tico.repository.UserRepository;
import com.femcoders.tico.service.EmailService;
import com.femcoders.tico.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ActivationFlowIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ActivationTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private EmailService emailService;
    @MockitoBean private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        when(rateLimiterService.tryConsume(anyString())).thenReturn(true);
    }

    @Test
    void activateAccount_withValidToken_userBecomesActiveAndPasswordIsSet() throws Exception {
        User user = saveUser("ana@test.com", false);
        saveToken(user, "ABC123", TokenType.ACTIVATION, false, LocalDateTime.now().plusMinutes(30));

        ActivationRequest dto = new ActivationRequest("ana@test.com", "ABC123", "newPassword1", "newPassword1");

        mockMvc.perform(post("/api/activation/activate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        User updated = userRepository.findByEmail("ana@test.com").orElseThrow();
        assertTrue(updated.getIsActive());
        assertTrue(passwordEncoder.matches("newPassword1", updated.getPasswordHash()));
    }

    @Test
    void activateAccount_withExpiredToken_returns400AndUserStaysInactive() throws Exception {
        User user = saveUser("carlos@test.com", false);
        saveToken(user, "EXP123", TokenType.ACTIVATION, false, LocalDateTime.now().minusMinutes(1));

        ActivationRequest dto = new ActivationRequest("carlos@test.com", "EXP123", "newPassword1", "newPassword1");

        mockMvc.perform(post("/api/activation/activate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        assertFalse(userRepository.findByEmail("carlos@test.com").orElseThrow().getIsActive());
    }

    @Test
    void resendCode_createsNewToken_withDifferentCode() throws Exception {
        User user = saveUser("maria@test.com", false);
        saveToken(user, "OLD123", TokenType.ACTIVATION, false, LocalDateTime.now().plusMinutes(30));

        mockMvc.perform(post("/api/activation/resend")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"maria@test.com\"}"))
                .andExpect(status().isOk());

        ActivationToken newToken = tokenRepository
                .findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("maria@test.com", TokenType.ACTIVATION)
                .orElseThrow();
        assertNotEquals("OLD123", newToken.getCode());
        assertEquals(6, newToken.getCode().length());
    }

    @Test
    void confirmReset_withValidToken_passwordChangesInDB() throws Exception {
        User user = saveUser("pedro@test.com", true);
        saveToken(user, "RST456", TokenType.RESET, false, LocalDateTime.now().plusMinutes(30));

        ResetPasswordConfirmRequest dto = new ResetPasswordConfirmRequest("pedro@test.com", "RST456", "brandNew99", "brandNew99");

        mockMvc.perform(post("/api/auth/password/confirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        User updated = userRepository.findByEmail("pedro@test.com").orElseThrow();
        assertTrue(passwordEncoder.matches("brandNew99", updated.getPasswordHash()));
    }

    private User saveUser(String email, boolean active) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash("hashed_temp");
        user.setIsActive(active);
        return userRepository.save(user);
    }

    private ActivationToken saveToken(User owner, String code, TokenType type,
                                      boolean used, LocalDateTime expiresAt) {
        ActivationToken token = new ActivationToken();
        token.setUser(owner);
        token.setCode(code);
        token.setType(type);
        token.setUsed(used);
        token.setExpiresAt(expiresAt);
        return tokenRepository.save(token);
    }
}
