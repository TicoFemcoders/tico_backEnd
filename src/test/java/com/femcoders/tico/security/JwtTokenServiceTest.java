package com.femcoders.tico.security;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.interfaces.DecodedJWT;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "jwtSecret", "test-secret-key-for-unit-tests-only");
        ReflectionTestUtils.setField(jwtTokenService, "jwtExpiration", 3600000L);
    }

    @Test
    void createToken_debeGenerarToken_cuandoDatosCorrectos() {
        String token = jwtTokenService.createToken("ana@test.com", 1L, Set.of("ROLE_ADMIN"));

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void verify_debeDecodificarToken_cuandoTokenValido() {
        String token = jwtTokenService.createToken("ana@test.com", 1L, Set.of("ROLE_ADMIN"));

        DecodedJWT decoded = jwtTokenService.verify(token);

        assertEquals("ana@test.com", decoded.getSubject());
        assertEquals(1L, decoded.getClaim("userId").asLong());
    }

    @Test
    void verify_debeIncluirRoles_enElToken() {
        String token = jwtTokenService.createToken("ana@test.com", 1L, Set.of("ROLE_ADMIN", "ROLE_EMPLOYEE"));

        DecodedJWT decoded = jwtTokenService.verify(token);
        String[] roles = decoded.getClaim("roles").asArray(String.class);

        assertNotNull(roles);
        assertTrue(Set.of(roles).contains("ROLE_ADMIN"));
    }

    @Test
    void verify_debeLanzarExcepcion_cuandoTokenInvalido() {
        assertThrows(Exception.class, () -> jwtTokenService.verify("token.invalido.aqui"));
    }

    @Test
    void verify_debeLanzarExcepcion_cuandoTokenFirmadoConOtraKey() {
        JwtTokenService otroService = new JwtTokenService();
        ReflectionTestUtils.setField(otroService, "jwtSecret", "otra-secret-key-diferente");
        ReflectionTestUtils.setField(otroService, "jwtExpiration", 3600000L);

        String tokenOtro = otroService.createToken("ana@test.com", 1L, Set.of("ROLE_ADMIN"));

        assertThrows(Exception.class, () -> jwtTokenService.verify(tokenOtro));
    }
}