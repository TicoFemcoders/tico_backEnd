package com.femcoders.tico.controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.tico.dto.request.AdminCreateUserRequest;
import com.femcoders.tico.dto.request.UpdateUserRequest;
import com.femcoders.tico.dto.response.UserResponse;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.exception.ExceptionResponseBuilder;
import com.femcoders.tico.security.CustomAuthenticationManager;
import com.femcoders.tico.security.JwtTokenService;
import com.femcoders.tico.security.SecurityConfig;
import com.femcoders.tico.service.UserService;


@WebMvcTest(UserController.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
@Import(SecurityConfig.class)
class UserControllerTest {

    @TestConfiguration
    static class Config {
        @Bean
        ExceptionResponseBuilder exceptionResponseBuilder() {
            return new ExceptionResponseBuilder();
        }
    }

    @MockitoBean private UserService userService;
    @MockitoBean private CustomAuthenticationManager customAuthenticationManager;
    @MockitoBean private JwtTokenService jwtTokenService;
    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private UserResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new UserResponse(1L, "Ana García", "ana@tico.com",
                Set.of("EMPLOYEE"), false, 0L, LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_whenValidRequest_returns201() throws Exception {
        AdminCreateUserRequest dto = new AdminCreateUserRequest(
                "Ana García", "ana@tico.com", Set.of(UserRole.EMPLOYEE));

        when(userService.createUser(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_whenInvalidRequest_returns400() throws Exception {
        String invalidJson = """
                {"name":"","email":"not-an-email","roles":["EMPLOYEE"]}
                """;

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_whenNotAuthenticated_returns401() throws Exception {
        AdminCreateUserRequest dto = new AdminCreateUserRequest(
                "Ana García", "ana@tico.com", Set.of(UserRole.EMPLOYEE));

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_whenExists_returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_whenValidRequest_returns200() throws Exception {
        UpdateUserRequest dto = new UpdateUserRequest(
                "Ana García", "ana@tico.com", Set.of(UserRole.EMPLOYEE));

        when(userService.updateUser(any(), any())).thenReturn(mockResponse);

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleUserActive_whenValidRequest_returns200() throws Exception {
        when(userService.toggleUserActive(any(), any())).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/users/1/active")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
