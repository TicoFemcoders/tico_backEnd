package com.femcoders.tico.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    @Test
    void tryConsume_ShouldReturnTrue_WhenFirstRequest() {
        assertTrue(rateLimiterService.tryConsume("user@test.com"));
    }

    @Test
    void tryConsume_ShouldReturnTrue_WhenWithinCapacityLimit() {
        String key = "user@test.com";

        assertTrue(rateLimiterService.tryConsume(key));
        assertTrue(rateLimiterService.tryConsume(key));
        assertTrue(rateLimiterService.tryConsume(key));
    }

    @Test
    void tryConsume_ShouldReturnFalse_WhenCapacityExceeded() {
        String key = "user@test.com";

        rateLimiterService.tryConsume(key);
        rateLimiterService.tryConsume(key);
        rateLimiterService.tryConsume(key);

        assertFalse(rateLimiterService.tryConsume(key));
    }

    @Test
    void tryConsume_ShouldReturnTrue_WhenDifferentKeysAreIndependent() {
        String key1 = "user1@test.com";
        String key2 = "user2@test.com";

        rateLimiterService.tryConsume(key1);
        rateLimiterService.tryConsume(key1);
        rateLimiterService.tryConsume(key1);

        assertTrue(rateLimiterService.tryConsume(key2));
    }

    @Test
    void tryConsume_ShouldReturnFalse_WhenDifferentKeysExceedTheirOwnLimit() {
        String key1 = "user1@test.com";
        String key2 = "user2@test.com";

        rateLimiterService.tryConsume(key1);
        rateLimiterService.tryConsume(key1);
        rateLimiterService.tryConsume(key1);
        assertFalse(rateLimiterService.tryConsume(key1));

        rateLimiterService.tryConsume(key2);
        rateLimiterService.tryConsume(key2);
        rateLimiterService.tryConsume(key2);
        assertFalse(rateLimiterService.tryConsume(key2));
    }

    @Test
    void tryConsume_ShouldReuseSameBucket_WhenSameKeyCalledMultipleTimes() {
        String key = "user@test.com";

        rateLimiterService.tryConsume(key);
        rateLimiterService.tryConsume(key);

        assertTrue(rateLimiterService.tryConsume(key));
        assertFalse(rateLimiterService.tryConsume(key));
    }
}
