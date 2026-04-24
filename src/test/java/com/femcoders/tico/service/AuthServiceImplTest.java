package com.femcoders.tico.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.femcoders.tico.dto.ResetPasswordConfirm;
import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.repository.ActivationTokenRepository;
import com.femcoders.tico.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ActivationTokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RateLimiterService rateLimiterService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Ana García");
        mockUser.setEmail("ana@test.com");
        mockUser.setIsActive(true);
        mockUser.setPasswordHash("hashedPassword");

        SecurityContextHolder.setContext(securityContext);
    }

    // ── getAuthenticatedUser ───────────────────────────────────────────────

    @Test
    void getAuthenticatedUser_debeRetornarUsuario_cuandoEstaAutenticado() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("ana@test.com");
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(mockUser));

        User result = authService.getAuthenticatedUser();

        assertNotNull(result);
        assertEquals("ana@test.com", result.getEmail());
    }

    @Test
    void getAuthenticatedUser_debeLanzarExcepcion_cuandoNoHayAutenticacion() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> authService.getAuthenticatedUser());
    }

    @Test
    void getAuthenticatedUser_debeLanzarExcepcion_cuandoUsuarioNoExiste() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("noexiste@test.com");
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getAuthenticatedUser());
    }

    // ── requestReset ──────────────────────────────────────────────────────

    @Test
    void requestReset_debeEnviarEmail_cuandoDatosCorrectos() {
        when(rateLimiterService.tryConsume("ana@test.com")).thenReturn(true);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(mockUser));
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.requestReset("ana@test.com");

        verify(emailService).sendResetEmail(eq("ana@test.com"), eq("Ana García"), anyString());
        verify(tokenRepository).save(any(ActivationToken.class));
    }

    @Test
    void requestReset_debeLanzarExcepcion_cuandoRateLimitSuperado() {
        when(rateLimiterService.tryConsume("ana@test.com")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.requestReset("ana@test.com"));
        verify(emailService, never()).sendResetEmail(any(), any(), any());
    }

    @Test
    void requestReset_debeLanzarExcepcion_cuandoUsuarioNoExiste() {
        when(rateLimiterService.tryConsume("noexiste@test.com")).thenReturn(true);
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.requestReset("noexiste@test.com"));
    }

    @Test
    void requestReset_debeLanzarExcepcion_cuandoCuentaNoActivada() {
        mockUser.setIsActive(false);
        when(rateLimiterService.tryConsume("ana@test.com")).thenReturn(true);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(mockUser));

        assertThrows(BadRequestException.class, () -> authService.requestReset("ana@test.com"));
    }

    // ── confirmReset ──────────────────────────────────────────────────────

    @Test
    void confirmReset_debeCambiarContrasena_cuandoDatosCorrectos() {
        ActivationToken token = new ActivationToken();
        token.setUser(mockUser);
        token.setCode("ABC123");
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        ResetPasswordConfirm dto = new ResetPasswordConfirm("ana@test.com", "ABC123", "nuevaPass123", "nuevaPass123");

        when(tokenRepository.findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@test.com", TokenType.RESET))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("nuevaPass123")).thenReturn("hashedNuevaPass");

        authService.confirmReset(dto);

        assertTrue(token.isUsed());
        assertEquals("hashedNuevaPass", mockUser.getPasswordHash());
        verify(userRepository).save(mockUser);
    }

    @Test
    void confirmReset_debeLanzarExcepcion_cuandoCodigoIncorrecto() {
        ActivationToken token = new ActivationToken();
        token.setUser(mockUser);
        token.setCode("ABC123");
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        ResetPasswordConfirm dto = new ResetPasswordConfirm("ana@test.com", "WRONG1", "nuevaPass123", "nuevaPass123");

        when(tokenRepository.findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@test.com", TokenType.RESET))
                .thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> authService.confirmReset(dto));
    }

    @Test
    void confirmReset_debeLanzarExcepcion_cuandoTokenExpirado() {
        ActivationToken token = new ActivationToken();
        token.setUser(mockUser);
        token.setCode("ABC123");
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(10));

        ResetPasswordConfirm dto = new ResetPasswordConfirm("ana@test.com", "ABC123", "nuevaPass123", "nuevaPass123");

        when(tokenRepository.findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@test.com", TokenType.RESET))
                .thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> authService.confirmReset(dto));
    }

    @Test
    void confirmReset_debeLanzarExcepcion_cuandoContrasenasNoCoinciden() {
        ActivationToken token = new ActivationToken();
        token.setUser(mockUser);
        token.setCode("ABC123");
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        ResetPasswordConfirm dto = new ResetPasswordConfirm("ana@test.com", "ABC123", "pass1", "pass2");

        when(tokenRepository.findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@test.com", TokenType.RESET))
                .thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> authService.confirmReset(dto));
    }
}