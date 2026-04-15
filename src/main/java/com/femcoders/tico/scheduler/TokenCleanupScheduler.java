package com.femcoders.tico.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.repository.ActivationTokenRepository;

@Component
public class TokenCleanupScheduler {

  private static final Logger log = LoggerFactory.getLogger(TokenCleanupScheduler.class);

  @Autowired
  private ActivationTokenRepository activationTokenRepository;

  @Scheduled(cron = "0 0 3 * * *")
  public void cleanExpiredTokens() {
    List<ActivationToken> toDelete =
        activationTokenRepository.findAllByUsedTrueOrExpiresAtBefore(LocalDateTime.now());

    if (!toDelete.isEmpty()) {
      activationTokenRepository.deleteAll(toDelete);
      log.info("[TokenCleanup] Eliminados {} tokens usados/expirados", toDelete.size());
    } else {
      log.info("[TokenCleanup] No hay tokens que limpiar");
    }
  }
}
