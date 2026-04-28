package com.femcoders.tico.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.repository.ActivationTokenRepository;

@ExtendWith(MockitoExtension.class)
class TokenCleanupSchedulerTest {

    @Mock
    private ActivationTokenRepository activationTokenRepository;

    @InjectMocks
    private TokenCleanupScheduler tokenCleanupScheduler;

    @Nested
    class CleanExpiredTokens {

        @Test
        void happyPath_tokensFound_deletesAll() {
            ActivationToken used = new ActivationToken();
            ActivationToken expired = new ActivationToken();
            List<ActivationToken> tokens = List.of(used, expired);

            when(activationTokenRepository.findAllByUsedTrueOrExpiresAtBefore(any(LocalDateTime.class)))
                    .thenReturn(tokens);

            tokenCleanupScheduler.cleanExpiredTokens();

            verify(activationTokenRepository).deleteAll(tokens);
        }

        @Test
        void happyPath_noTokensFound_deleteAllIsNeverCalled() {
            when(activationTokenRepository.findAllByUsedTrueOrExpiresAtBefore(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            tokenCleanupScheduler.cleanExpiredTokens();

            verify(activationTokenRepository, never()).deleteAll(any());
        }

        @Test
        void happyPath_singleToken_deletesIt() {
            ActivationToken token = new ActivationToken();
            List<ActivationToken> tokens = List.of(token);

            when(activationTokenRepository.findAllByUsedTrueOrExpiresAtBefore(any(LocalDateTime.class)))
                    .thenReturn(tokens);

            tokenCleanupScheduler.cleanExpiredTokens();

            verify(activationTokenRepository).deleteAll(tokens);
        }
    }
}
