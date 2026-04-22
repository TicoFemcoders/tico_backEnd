package com.femcoders.tico.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.femcoders.tico.dto.request.ActivationRequest;
import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.repository.ActivationTokenRepository;
import com.femcoders.tico.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ActivationServiceImpl implements ActivationService {

  private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private final ActivationTokenRepository tokenRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  @Override
  @Transactional
  public void activateAccount(ActivationRequest dto) {
    ActivationToken token = tokenRepository
        .findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(dto.email(), TokenType.ACTIVATION)
        .orElseThrow(() -> new ResourceNotFoundException("Token", "email", dto.email()));

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
    user.setIsActive(true);
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void resendCode(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

    if (Boolean.TRUE.equals(user.getIsActive())) {
      throw new IllegalStateException("La cuenta ya está activada");
    }

    String code = generateCodeAndSaveToken(user, TokenType.ACTIVATION);
    emailService.sendActivationEmail(user.getEmail(), user.getName(), code);
  }

  @Override
  public String generateCodeAndSaveToken(User user, TokenType type) {
    String code = generateCode();
    ActivationToken token = new ActivationToken();
    token.setUser(user);
    token.setCode(code);
    token.setType(type);
    token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
    tokenRepository.save(token);
    return code;
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
