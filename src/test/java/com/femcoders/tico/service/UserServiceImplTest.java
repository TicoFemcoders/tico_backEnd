package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.AdminCreateUserRequest;
import com.femcoders.tico.dto.response.UserResponse;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.mapper.UserMapper;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ActivationService activationService;

    @Mock
    private EmailService emailService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse mockResponse;
    private AdminCreateUserRequest createRequest;


    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Ana García");
        user.setEmail("ana@tico.com");
        user.setIsActive(false);

        mockResponse = new UserResponse(1L, "Ana García", "ana@tico.com", Set.of("EMPLOYEE"), false, 0L, LocalDateTime.now());
        createRequest = new AdminCreateUserRequest("Ana García", "ana@tico.com", Set.of(com.femcoders.tico.enums.UserRole.EMPLOYEE));
    }

    @Test
    void createUser_whenAllIsOk_generatesTokenAndSendsEmail() {
        when(userMapper.toEntity(createRequest)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_temp_password");
        when(userRepository.save(user)).thenReturn(user);
        when(activationService.generateCodeAndSaveToken(user, TokenType.ACTIVATION)).thenReturn("ABC123");
        when(userMapper.toResponseDTO(user)).thenReturn(mockResponse);

        UserResponse result = userService.createUser(createRequest);

        assertNotNull(result);

        verify(activationService).generateCodeAndSaveToken(user, TokenType.ACTIVATION);
        verify(emailService).sendActivationEmail("ana@tico.com", "Ana García", "ABC123");
    }

    @Test
    void createUser_whenEmailFails_stillCreatesUser() {
        when(userMapper.toEntity(createRequest)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_temp_password");
        when(userRepository.save(user)).thenReturn(user);
        when(activationService.generateCodeAndSaveToken(user, TokenType.ACTIVATION)).thenReturn("ABC123");

        doThrow(new RuntimeException("SMTP caído")).when(emailService)
                .sendActivationEmail(anyString(), anyString(), anyString());

        when(userMapper.toResponseDTO(user)).thenReturn(mockResponse);

        UserResponse result = assertDoesNotThrow(() -> userService.createUser(createRequest));

        assertNotNull(result);

        verify(userRepository).save(user);
    }
}
