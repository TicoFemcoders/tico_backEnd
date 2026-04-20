package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;

public record TicketMessageResponseDTO(
                Long id,
                Long ticketId,
                String authorName,
                String content,
                Boolean isInternal,
                Boolean isRead,
                LocalDateTime createdAt) {
}