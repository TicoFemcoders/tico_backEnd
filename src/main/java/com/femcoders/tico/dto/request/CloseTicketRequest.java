package com.femcoders.tico.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CloseTicketRequest(
        @NotBlank String closingMessage
) {}
