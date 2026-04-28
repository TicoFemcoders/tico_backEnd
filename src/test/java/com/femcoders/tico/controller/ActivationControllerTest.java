package com.femcoders.tico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.tico.dto.request.ActivationRequest;
import com.femcoders.tico.exception.ExceptionResponseBuilder;
import com.femcoders.tico.security.CustomAuthenticationManager;
import com.femcoders.tico.security.JwtTokenService;
import com.femcoders.tico.service.ActivationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ActivationController.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
@WithMockUser
class ActivationControllerTest {

    @TestConfiguration
    static class Config {
        @Bean
        ExceptionResponseBuilder exceptionResponseBuilder() {
            return new ExceptionResponseBuilder();
        }
    }

    @MockitoBean
    private ActivationService activationService;

    @MockitoBean
    private CustomAuthenticationManager customAuthenticationManager;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void activate_whenValidRequest_returns200() throws Exception {
        ActivationRequest dto = new ActivationRequest("ana@tico.com", "ABC123", "password123", "password123");

        mockMvc.perform(post("/api/activation/activate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void activate_whenEmailIsBlank_returns400() throws Exception {
        String invalidJson = """
                {"email":"","code":"ABC123","password":"password123","confirmPassword":"password123"}
                """;

        mockMvc.perform(post("/api/activation/activate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_whenCodeIsTooShort_returns400() throws Exception {
        String invalidJson = """
                {"email":"ana@tico.com","code":"ABC","password":"password123","confirmPassword":"password123"}
                """;

        mockMvc.perform(post("/api/activation/activate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }


    @Test
    void resend_whenValidEmail_returns200() throws Exception {
        String validJson = "{\"email\":\"ana@tico.com\"}";

        mockMvc.perform(post("/api/activation/resend")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void resend_whenEmailFormatIsInvalid_returns400() throws Exception {
        String invalidJson = "{\"email\":\"not-an-email\"}";

        mockMvc.perform(post("/api/activation/resend")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
