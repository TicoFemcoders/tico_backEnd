package com.femcoders.tico.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ActivationTokenTest {

    @Test
    void newToken_usedIsFalseByDefault() {
        ActivationToken token = new ActivationToken();

        assertFalse(token.isUsed());
    }
}
