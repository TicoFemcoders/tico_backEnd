package com.femcoders.tico.repository;

import com.femcoders.tico.entity.ActivationToken;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ActivationTokenRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ActivationTokenRepository tokenRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Ana García");
        user.setEmail("ana@tico.com");
        user.setPasswordHash("hashed_password");
        em.persist(user);
        em.flush();
    }

    @Test
    void findFirst_whenValidUnusedTokenExists_returnsIt() {
        ActivationToken token = buildToken(user, "ABC123", TokenType.ACTIVATION, false, LocalDateTime.now().plusMinutes(20));
        em.persist(token);
        em.flush();

        Optional<ActivationToken> result = tokenRepository
                .findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@tico.com", TokenType.ACTIVATION);

        assertTrue(result.isPresent());
        assertEquals("ABC123", result.get().getCode());
    }

    @Test
    void findFirst_whenTokenIsUsed_returnsEmpty() {
        ActivationToken token = buildToken(user, "ABC123", TokenType.ACTIVATION, true, LocalDateTime.now().plusMinutes(20));
        em.persist(token);
        em.flush();

        Optional<ActivationToken> result = tokenRepository
                .findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@tico.com", TokenType.ACTIVATION);

        assertFalse(result.isPresent());
    }

    @Test
    void findFirst_whenTypeDoesNotMatch_returnsEmpty() {
        ActivationToken token = buildToken(user, "ABC123", TokenType.ACTIVATION, false, LocalDateTime.now().plusMinutes(20));
        em.persist(token);
        em.flush();

        Optional<ActivationToken> result = tokenRepository
                .findFirstByUserEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("ana@tico.com", TokenType.RESET);

        assertFalse(result.isPresent());
    }


    @Test
    void invalidatePendingTokens_setsAllActiveTokensAsUsed() {
        ActivationToken token1 = buildToken(user, "TOKEN1", TokenType.ACTIVATION, false, LocalDateTime.now().plusMinutes(20));
        ActivationToken token2 = buildToken(user, "TOKEN2", TokenType.ACTIVATION, false, LocalDateTime.now().plusMinutes(30));
        em.persist(token1);
        em.persist(token2);
        em.flush();

        tokenRepository.invalidatePendingTokens(user.getId(), TokenType.ACTIVATION);
        em.clear();

        ActivationToken updated1 = em.find(ActivationToken.class, token1.getId());
        ActivationToken updated2 = em.find(ActivationToken.class, token2.getId());
        assertTrue(updated1.isUsed());
        assertTrue(updated2.isUsed());
    }

    @Test
    void invalidatePendingTokens_doesNotAffectOtherUsersTokens() {
        User otherUser = new User();
        otherUser.setName("Carlos");
        otherUser.setEmail("carlos@tico.com");
        otherUser.setPasswordHash("hashed");
        em.persist(otherUser);

        ActivationToken anaToken    = buildToken(user,      "ANA111", TokenType.ACTIVATION, false, LocalDateTime.now().plusMinutes(20));
        ActivationToken carlosToken = buildToken(otherUser, "CAR111", TokenType.ACTIVATION, false, LocalDateTime.now().plusMinutes(20));
        em.persist(anaToken);
        em.persist(carlosToken);
        em.flush();

        tokenRepository.invalidatePendingTokens(user.getId(), TokenType.ACTIVATION);
        em.clear();

        ActivationToken updatedCarlos = em.find(ActivationToken.class, carlosToken.getId());
        assertFalse(updatedCarlos.isUsed());
    }


    @Test
    void findAllExpiredOrUsed_returnsOnlyExpiredAndUsedTokens() {
        ActivationToken usedToken    = buildToken(user, "USED11", TokenType.ACTIVATION, true,  LocalDateTime.now().plusMinutes(20)); // ya usado
        ActivationToken expiredToken = buildToken(user, "EXPIR1", TokenType.ACTIVATION, false, LocalDateTime.now().minusMinutes(1)); // expirado
        ActivationToken validToken   = buildToken(user, "VALID1", TokenType.RESET,      false, LocalDateTime.now().plusMinutes(20)); // todavía válido
        em.persist(usedToken);
        em.persist(expiredToken);
        em.persist(validToken);
        em.flush();

        List<ActivationToken> result = tokenRepository.findAllByUsedTrueOrExpiresAtBefore(LocalDateTime.now());

        assertEquals(2, result.size());
    }

    private ActivationToken buildToken(User owner, String code, TokenType type, boolean used, LocalDateTime expiresAt) {
        ActivationToken token = new ActivationToken();
        token.setUser(owner);
        token.setCode(code);
        token.setType(type);
        token.setUsed(used);
        token.setExpiresAt(expiresAt);
        return token;
    }
}
