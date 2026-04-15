package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;

public record TicketMessageResponseDTO(
    Long id,
    Long ticketId,
    Long authorId,
    String content,
    Boolean isInternal,
    LocalDateTime createdAt
) {}