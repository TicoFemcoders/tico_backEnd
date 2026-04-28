package com.femcoders.tico.dto.request;

import java.util.Set;

import com.femcoders.tico.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
        @NotBlank(message = "Se requiere el nombre") @Size(min = 2, max = 30)
        String name,

        @NotBlank(message = "Se requiere correo electrónico") @Email(message = "Formato de correo electrónico no válido")
        String email,

        @NotNull @Size(min = 1, message = "Se requiere al menos un rol")
        Set<UserRole> roles

) {

}
