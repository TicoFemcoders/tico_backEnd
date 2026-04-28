package com.femcoders.tico.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.femcoders.tico.service.UserService;

@Component
public class CustomAuthenticationManager implements AuthenticationManager {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  public CustomAuthenticationManager(UserService userservice, PasswordEncoder passwordEncoder) {
    this.userService = userservice;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    UserDetail userDetail =(UserDetail) userService.loadUserByUsername(authentication.getName());

    if (!userDetail.isEnabled()) {
      throw new DisabledException("La cuenta no está activa. Contacta con un administrador.");
    }

    if (!passwordEncoder.matches(authentication.getCredentials().toString(), userDetail.getPassword())) {
      throw new BadCredentialsException("La contraseña es incorrecta");
    }

    return new UsernamePasswordAuthenticationToken(
        userDetail,
        userDetail.getPassword(),
        userDetail.getAuthorities());
  }
}