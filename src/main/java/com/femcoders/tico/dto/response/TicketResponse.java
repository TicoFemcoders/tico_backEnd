package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        String createdByName,
        Long assignedToId,
        String assignedToName,
        Set<LabelSummary> labels,
        String emailSubject,
        String closingMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt) {
}