package com.femcoders.tico.dto.request;

import com.femcoders.tico.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegisterReqDTO(

@NotBlank(message = "name is required")
@Size(min = 2, max = 30 )
String name,

@NotBlank(message = "Email is required ")
@Email(message = "Invalid email format")
String email,

@NotBlank(message = "Password is required")
@Size(min = 8, message = "Password must be 8 characters long")
String password,

@NotNull(message = "Rol is required")
UserRole role


) {

}
