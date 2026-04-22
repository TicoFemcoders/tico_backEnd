package com.femcoders.tico.security;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

@Service
public class JwtTokenService {

  @Value("${JWT_SECRET}")
  private String jwtSecret;

  @Value("${JWT_EXPIRATION}")
  private long jwtExpiration;

  public String createToken(String email, Long userId, Set<String> roles) {
    return JWT.create()
        .withSubject(email)
        .withClaim("userId", userId)
        .withArrayClaim("roles", roles.toArray(new String[0]))
        .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
        .sign(Algorithm.HMAC256(jwtSecret));
  }

  public DecodedJWT verify(String token) {
    return JWT.require(Algorithm.HMAC256(jwtSecret))
        .build()
        .verify(token);
  }
}