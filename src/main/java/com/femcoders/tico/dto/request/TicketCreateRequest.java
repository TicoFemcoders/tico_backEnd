package com.femcoders.tico.dto.request;

import java.util.List;

import com.femcoders.tico.enums.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(
        @NotBlank(message = "El campo asunto es obligatorio")
        @Size(min = 5, max = 100, message = "El asunto debe tener entre 5 y 100 caracteres.")
        String title,
        @NotBlank(message = "El campo descripción es obligatorio")
        @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres.")
        String description,
        @NotNull(message = "Se requiere seleccionar prioridad")
        TicketPriority priority,
        List<Long> labelIds
        ) {

}
