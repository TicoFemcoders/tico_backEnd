package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;

public record TicketResponseDTO(
                Long id,
                String title,
                String description,
                TicketStatus status,
                TicketPriority priority,
                Long createdById,
                Long assignedToId,
                Set<String> labels,
                String emailSubject,
                String closingMessage,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                LocalDateTime closedAt) {
}