package com.femcoders.tico.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.femcoders.tico.dto.ResetPasswordConfirm;
import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.repository.ActivationTokenRepository;
import com.femcoders.tico.repository.UserRepository;
import com.femcoders.tico.security.UserDetail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UserRepository userRepository;
    private final ActivationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("No autorizado: Debes estar logueado.");
        }
        String email = authentication.getPrincipal().toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
    }

    @Override
    public User getOptionalAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserDetail) {

            UserDetail userDetail = (UserDetail) authentication.getPrincipal();
            return userRepository.findById(userDetail.getUser().getId()).orElse(null);
        }
        return null;
    }

    @Override
    @Transactional
    public void requestReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Activa tu cuenta antes de resetear la contraseña");
        }

        String code = generateCode();
        ActivationToken token = new ActivationToken();
        token.setUser(user);
        token.setCode(code);
        token.setType(TokenType.RESET);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(token);

        emailService.sendResetEmail(user.getEmail(), user.getName(), code);
    }

    @Override
    @Transactional
    public void confirmReset(ResetPasswordConfirm dto) {
        ActivationToken token = tokenRepository
                .findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(dto.email(), TokenType.RESET)
                .orElseThrow(() -> new ResourceNotFoundException("Token de reset", "email", dto.email()));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Código expirado");
        }

        if (!token.getCode().equals(dto.code())) {
            throw new BadRequestException("Código incorrecto");
        }

        if (!dto.password().equals(dto.confirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        token.setUsed(true);
        tokenRepository.save(token);

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        userRepository.save(user);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}