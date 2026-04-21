package com.femcoders.tico.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.request.ActivationReqDTO;
import com.femcoders.tico.dto.request.ResendCodeRequestDTO;
import com.femcoders.tico.service.ActivationService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/activation")
@RequiredArgsConstructor
public class ActivationController {

  private final ActivationService activationService;

  @PostMapping("/activate")
  public ResponseEntity<Void> activate(@Valid @RequestBody ActivationReqDTO dto) {
    activationService.activateAccount(dto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/resend")
  public ResponseEntity<Void> resend(@Valid @RequestBody ResendCodeRequestDTO dto) {
    activationService.resendCode(dto.email());
    return ResponseEntity.ok().build();
  }
}
