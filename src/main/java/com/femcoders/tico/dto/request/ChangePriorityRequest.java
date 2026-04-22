package com.femcoders.tico.dto.request;

import com.femcoders.tico.enums.TicketPriority;
import jakarta.validation.constraints.NotNull;

public record ChangePriorityRequest(
        @NotNull TicketPriority priority
) {}
