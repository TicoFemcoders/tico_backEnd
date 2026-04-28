package com.femcoders.tico.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.security.CustomAuthenticationManager;
import com.femcoders.tico.security.JwtTokenService;
import com.femcoders.tico.security.UserDetail;

@ExtendWith(MockitoExtension.class)
class JWTAuthenticationFilterTest {

    @Mock
    private CustomAuthenticationManager authenticationManager;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private FilterChain chain;

    private JWTAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JWTAuthenticationFilter(authenticationManager, jwtTokenService);
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Ana");
        user.setEmail("ana@test.com");
        user.setRoles(Set.of(UserRole.EMPLOYEE));
        user.setPasswordHash("hash");
        user.setIsActive(true);
        return user;
    }

    @Nested
    class AttemptAuthentication {

        @Test
        void happyPath_validJson_callsAuthManagerWithEmailAndPassword() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setContent("{\"email\":\"ana@test.com\",\"password\":\"secret\"}".getBytes());

            Authentication expected = mock(Authentication.class);
            when(authenticationManager.authenticate(any())).thenReturn(expected);

            Authentication result = filter.attemptAuthentication(request, response);

            ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
            verify(authenticationManager).authenticate(captor.capture());
            assertThat(captor.getValue().getPrincipal()).isEqualTo("ana@test.com");
            assertThat(captor.getValue().getCredentials()).isEqualTo("secret");
            assertThat(result).isEqualTo(expected);
        }

        @Test
        void sadPath_malformedJson_throwsAuthenticationServiceException() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setContent("not-valid-json".getBytes());

            assertThatThrownBy(() -> filter.attemptAuthentication(request, response))
                    .isInstanceOf(AuthenticationServiceException.class)
                    .hasMessageContaining("Error de lectura al identificar");
        }
    }

    @Nested
    class UnsuccessfulAuthentication {

        @Test
        void sadPath_sends401WithErrorMessage() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.unsuccessfulAuthentication(request, response,
                    new BadCredentialsException("La contraseña es incorrecta"));

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
            assertThat(response.getErrorMessage()).isEqualTo("La contraseña es incorrecta");
        }
    }

    @Nested
    class SuccessfulAuthentication {

        @Test
        void happyPath_addsTokenToHeaderAndWritesAuthResponseBody() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            UserDetail userDetail = new UserDetail(buildUser());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetail, "hash", userDetail.getAuthorities());

            when(jwtTokenService.createToken(anyString(), anyLong(), anySet()))
                    .thenReturn("generated.jwt.token");

            filter.successfulAuthentication(request, response, chain, authentication);

            assertThat(response.getHeader("Authorization")).isEqualTo("Bearer generated.jwt.token");
            assertThat(response.getContentType()).startsWith("application/json");
            String body = response.getContentAsString();
            assertThat(body).contains("ana@test.com");
            assertThat(body).contains("Ana");
        }
    }
}
