package com.femcoders.tico.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.request.ResetPasswordConfirmDTO;
import com.femcoders.tico.dto.request.ResetPasswordReqDTO;
import com.femcoders.tico.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/reset-password")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/request")
  public ResponseEntity<Void> requestReset(@Valid @RequestBody ResetPasswordReqDTO dto) {
    authService.requestReset(dto.email());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/confirm")
  public ResponseEntity<Void> confirmReset(@Valid @RequestBody ResetPasswordConfirmDTO dto) {
    authService.confirmReset(dto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/resend")
  public ResponseEntity<Void> resendReset(@Valid @RequestBody ResetPasswordReqDTO dto) {
    authService.requestReset(dto.email());
    return ResponseEntity.ok().build();
  }
}
