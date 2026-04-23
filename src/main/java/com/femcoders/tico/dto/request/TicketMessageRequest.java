package com.femcoders.tico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketMessageRequest(

        @NotNull Long ticketId,

        @NotBlank String content,

        Long recipientId

) {
}