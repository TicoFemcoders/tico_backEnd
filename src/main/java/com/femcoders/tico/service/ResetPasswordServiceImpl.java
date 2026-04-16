package com.femcoders.tico.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.femcoders.tico.dto.request.ResetPasswordConfirmDTO;
import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.repository.ActivationTokenRepository;
import com.femcoders.tico.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ResetPasswordServiceImpl implements ResetPasswordService {

  private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ActivationTokenRepository tokenRepository;

  @Autowired
  private EmailService emailService;

  @Autowired
  private PasswordEncoder passwordEncoder;

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
  public void confirmReset(ResetPasswordConfirmDTO dto) {
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
