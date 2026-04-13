package com.femcoders.tico.dto.request;

import com.femcoders.tico.enums.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateReqDTO {

    @NotBlank(message = "El campo asunto es obligatorio")
    @Size(min = 5, max = 100, message = "El asunto debe tener entre 5 y 100 caracteres.")
    private String title;

    @NotBlank(message = "El campo descripción es obligatorio")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres.")
    private String description;

    @NotNull(message = "Se requiere seleccionar prioridad")
    private TicketPriority priority;
}