package com.femcoders.tico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordConfirm(
    @NotBlank String email,
    @NotBlank @Size(min = 6, max = 6) String code,
    @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password,
    @NotBlank String confirmPassword) {
}
