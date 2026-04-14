package com.femcoders.tico.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.enums.TokenType;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {

  Optional<ActivationToken> findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(String email, TokenType type);

  List<ActivationToken> findAllByUsedTrueOrExpiresAtBefore(LocalDateTime now);

}
