package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;

public record TicketMessageResponse(
        Long id,
        Long ticketId,
        String authorName,
        String content,
        Boolean isRead,
        LocalDateTime createdAt,
        Long recipientId) {
}