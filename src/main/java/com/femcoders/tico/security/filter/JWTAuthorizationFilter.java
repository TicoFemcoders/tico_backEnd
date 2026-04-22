package com.femcoders.tico.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.femcoders.tico.security.JwtTokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

  private final JwtTokenService jwtTokenService;

  public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
    super(authenticationManager);
    this.jwtTokenService = jwtTokenService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = header.replace("Bearer ", "");

    try {
      DecodedJWT decodedJWT = jwtTokenService.verify(token);

      String username = decodedJWT.getSubject();

      List<GrantedAuthority> authorities = decodedJWT.getClaim("roles").asList(String.class)
          .stream()
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList());

      Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

      SecurityContextHolder.getContext().setAuthentication(authentication);

      chain.doFilter(request, response);

    } catch (JWTVerificationException e) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no válido o expirado");
    }
  }
}
