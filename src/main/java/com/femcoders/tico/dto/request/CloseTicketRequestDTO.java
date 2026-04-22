package com.femcoders.tico.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CloseTicketRequestDTO(
        @NotBlank String closingMessage
) {}
