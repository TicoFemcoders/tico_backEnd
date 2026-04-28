package com.femcoders.tico.integration;

import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class LoginFlowIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void login_withCorrectCredentials_returns200AndJwtInHeader() throws Exception {
        saveActiveUser("ana@test.com", "Password1!", UserRole.EMPLOYEE);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@test.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(header().string("Authorization", org.hamcrest.Matchers.startsWith("Bearer ")))
                .andExpect(jsonPath("$.email").value("ana@test.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        saveActiveUser("carlos@test.com", "correct-pass", UserRole.EMPLOYEE);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"carlos@test.com\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withInactiveUser_returns401() throws Exception {
        saveInactiveUser("inactive@test.com", "Password1!");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"inactive@test.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withNonExistentEmail_returns401() throws Exception {
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ghost@test.com\",\"password\":\"anypassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    private void saveActiveUser(String email, String rawPassword, UserRole role) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setIsActive(true);
        user.getRoles().add(role);
        userRepository.save(user);
    }

    private void saveInactiveUser(String email, String rawPassword) {
        User user = new User();
        user.setName("Inactive User");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setIsActive(false);
        user.getRoles().add(UserRole.EMPLOYEE);
        userRepository.save(user);
    }
}
