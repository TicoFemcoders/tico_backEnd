package com.femcoders.tico.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordReqDTO(
        @NotBlank(message = "Se requiere correo electrónico") @Email(message = "Formato de correo electrónico no válido") String email) {

}
