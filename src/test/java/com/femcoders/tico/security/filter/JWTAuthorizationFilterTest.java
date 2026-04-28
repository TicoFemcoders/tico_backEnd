package com.femcoders.tico.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.femcoders.tico.security.JwtTokenService;

@ExtendWith(MockitoExtension.class)
class JWTAuthorizationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private FilterChain chain;

    private JWTAuthorizationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JWTAuthorizationFilter(authenticationManager, jwtTokenService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class DoFilterInternal {

        @Test
        void happyPath_noAuthorizationHeader_chainContinuesWithoutAuth() throws Exception {
            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(jwtTokenService, never()).verify(any());
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        void happyPath_headerWithoutBearerPrefix_chainContinues() throws Exception {
            request.addHeader("Authorization", "Basic somebase64token");

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(jwtTokenService, never()).verify(any());
        }

        @Test
        void happyPath_validBearerToken_setsSecurityContextAndChainContinues() throws Exception {
            DecodedJWT decodedJWT = mock(DecodedJWT.class);
            Claim claim = mock(Claim.class);

            request.addHeader("Authorization", "Bearer validtoken");
            when(jwtTokenService.verify("validtoken")).thenReturn(decodedJWT);
            when(decodedJWT.getSubject()).thenReturn("user@test.com");
            when(decodedJWT.getClaim("roles")).thenReturn(claim);
            when(claim.asList(String.class)).thenReturn(List.of("ROLE_EMPLOYEE"));

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getName()).isEqualTo("user@test.com");
            assertThat(auth.getAuthorities()).hasSize(1);
        }

        @Test
        void sadPath_invalidToken_returns401AndChainDoesNotContinue() throws Exception {
            request.addHeader("Authorization", "Bearer badtoken");
            when(jwtTokenService.verify("badtoken")).thenThrow(new JWTVerificationException("invalid token"));

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
            verify(chain, never()).doFilter(any(), any());
        }
    }
}
