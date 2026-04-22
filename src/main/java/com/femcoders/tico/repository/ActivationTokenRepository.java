package com.femcoders.tico.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.enums.TokenType;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {

  Optional<ActivationToken> findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(String email, TokenType type);

  List<ActivationToken> findAllByUsedTrueOrExpiresAtBefore(LocalDateTime now);

  @Modifying
  @Transactional
  @Query("UPDATE ActivationToken t SET t.used = true " +
         "WHERE t.user.id = :userId AND t.type = :type AND t.used = false")
  int invalidatePendingTokens(@Param("userId") Long userId, @Param("type") TokenType type);

}
