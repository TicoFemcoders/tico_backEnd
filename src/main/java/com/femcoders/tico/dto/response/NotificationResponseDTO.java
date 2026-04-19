package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
    Long id,
    Long ticketId,
    String Content,
    Boolean isRead,
    LocalDateTime createdAt) {

}
