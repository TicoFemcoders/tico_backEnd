package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.ActivationRequest;
import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.repository.ActivationTokenRepository;
import com.femcoders.tico.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationServiceImplTest {

    @Mock
    private ActivationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ActivationServiceImpl activationService;

    private User user;
    private ActivationToken validToken;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L); 
        user.setName("Ana García");
        user.setEmail("ana@tico.com");
        user.setIsActive(false);

        validToken = new ActivationToken();
        validToken.setUser(user); 
        validToken.setCode("ABC123");
        validToken.setType(TokenType.ACTIVATION);
        validToken.setExpiresAt(LocalDateTime.now().plusMinutes(20)); 
        validToken.setUsed(false); 
    }

    @Test
    void activateAccount_whenAllIsOk_activateAccount() {

        ActivationRequest dto = new ActivationRequest("ana@tico.com", "ABC123", "password123", "password123");

        when(tokenRepository.findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@tico.com", TokenType.ACTIVATION))
                .thenReturn(Optional.of(validToken));

        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");

        activationService.activateAccount(dto);
        assertTrue(validToken.isUsed());
        assertTrue(user.getIsActive());
        assertEquals("hashed_password", user.getPasswordHash());

        verify(tokenRepository).save(validToken);
        verify(userRepository).save(user);
    }

    @Test
    void activateAccount_whenTokenNotFound_throwsNotFoundException() {
        ActivationRequest dto = new ActivationRequest("noexiste@tico.com", "ABC123", "password123", "password123");

        when(tokenRepository.findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("noexiste@tico.com", TokenType.ACTIVATION))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> activationService.activateAccount(dto));
    }

    @Test
    void activateAccount_whenTokenExpired_throwsBadRequest() {
        validToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        ActivationRequest dto = new ActivationRequest("ana@tico.com", "ABC123", "password123", "password123");

        when(tokenRepository.findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@tico.com", TokenType.ACTIVATION))
                .thenReturn(Optional.of(validToken));
        assertThrows(BadRequestException.class, () -> activationService.activateAccount(dto));
    }

    @Test
    void activateAccount_whenCodeIsWrong_throwsBadRequest() {

        ActivationRequest dto = new ActivationRequest("ana@tico.com", "WRONG1", "password123", "password123");

        when(tokenRepository.findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@tico.com", TokenType.ACTIVATION))
                .thenReturn(Optional.of(validToken));

        assertThrows(BadRequestException.class, () -> activationService.activateAccount(dto));
    }

    @Test
    void activateAccount_whenPasswordsDontMatch_throwsBadRequest() {

        ActivationRequest dto = new ActivationRequest("ana@tico.com", "ABC123", "password123", "diferente456");

        when(tokenRepository.findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@tico.com", TokenType.ACTIVATION))
                .thenReturn(Optional.of(validToken));

        assertThrows(BadRequestException.class, () -> activationService.activateAccount(dto));
    }


    @Test
    void resendCode_whenUserNotFound_throwsNotFoundException() {
        when(userRepository.findByEmail("noexiste@tico.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> activationService.resendCode("noexiste@tico.com"));
    }

    @Test
    void resendCode_whenAccountAlreadyActive_throwsIllegalState() {
        user.setIsActive(true);

        when(userRepository.findByEmail("ana@tico.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> activationService.resendCode("ana@tico.com"));
    }

    @Test
    void resendCode_whenAccountInactive_invalidatesOldTokensAndSendsEmail() {
        when(userRepository.findByEmail("ana@tico.com")).thenReturn(Optional.of(user));

        activationService.resendCode("ana@tico.com");

        verify(tokenRepository).invalidatePendingTokens(user.getId(), TokenType.ACTIVATION);
        verify(tokenRepository).save(any(ActivationToken.class));
        verify(emailService).sendActivationEmail(eq("ana@tico.com"), eq("Ana García"), anyString());
    }

    @Test
    void generateCodeAndSaveToken_returns6CharCode() {
        String code = activationService.generateCodeAndSaveToken(user, TokenType.ACTIVATION);

        assertNotNull(code);

        assertEquals(6, code.length());

        verify(tokenRepository).save(any(ActivationToken.class));
    }
}
