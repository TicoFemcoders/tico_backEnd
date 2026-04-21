package com.femcoders.tico.dto.request;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserReqDTO(
    @NotBlank(message = "Se requiere el nombre") @Size(min = 2, max = 30) String name,

    @NotBlank(message = "Se requiere correo electrónico") @Email(message = "Formato de correo electrónico no válido") String email,

    @NotNull(message = "Se requiere al menos un rol") Set<String> roles) {

}
