package com.femcoders.tico.security.filter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.femcoders.tico.dto.request.LoginRequest;
import com.femcoders.tico.dto.response.AuthResponse;
import com.femcoders.tico.security.CustomAuthenticationManager;
import com.femcoders.tico.security.JwtTokenService;
import com.femcoders.tico.security.UserDetail;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final CustomAuthenticationManager authenticationManager;
  private final JwtTokenService jwtTokenService;

  public JWTAuthenticationFilter(CustomAuthenticationManager authenticationManager,
      JwtTokenService jwtTokenService) {
    super(authenticationManager);
    this.authenticationManager = authenticationManager;
    this.jwtTokenService = jwtTokenService;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException {
    try {
      LoginRequest loginRequest = new ObjectMapper()
          .readValue(request.getInputStream(), LoginRequest.class);

      Authentication authentication = new UsernamePasswordAuthenticationToken(
          loginRequest.email(),
          loginRequest.password());

      return authenticationManager.authenticate(authentication);

    } catch (IOException e) {
      throw new AuthenticationServiceException("Error de lectura al identificar", e);
    }
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException failed) throws IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, failed.getMessage());
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authentication) throws IOException {

    UserDetail userDetail = (UserDetail) authentication.getPrincipal();

    Set<String> roles = userDetail.getAuthorities().stream()
        .map(auth -> auth.getAuthority())
        .collect(Collectors.toSet());

    String token = jwtTokenService.createToken(
        userDetail.getUsername(),
        userDetail.getUser().getId(),
        roles);

    AuthResponse authResponse = new AuthResponse(
        userDetail.getUser().getId(),
        userDetail.getUser().getName(),
        userDetail.getUser().getEmail(),
        roles);

    response.addHeader("Authorization", "Bearer " + token);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    new ObjectMapper().writeValue(response.getWriter(), authResponse);
  }
}