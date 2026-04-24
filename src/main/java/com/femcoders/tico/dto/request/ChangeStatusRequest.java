package com.femcoders.tico.dto.request;

import com.femcoders.tico.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
        @NotNull TicketStatus status
) {}
