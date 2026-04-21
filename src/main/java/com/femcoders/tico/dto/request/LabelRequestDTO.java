package com.femcoders.tico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LabelRequestDTO(

    @NotBlank(message = "Se requiere el nombre")
    @Size(min = 2, max = 100,message = "El nombre debe tener entre 2 y 100 caracteres")
    String name,

    @NotBlank(message = "Se requiere color")
    @Pattern(
        regexp = "^#[A-Fa-f0-9]{6}$", 
        message = "El color debe tener un formato hexadecimal válido (ejemplo: #FF5733)"
    )
    String color
) {

}
