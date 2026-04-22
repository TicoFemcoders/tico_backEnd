package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    Long ticketId,
    String content,
    Boolean isRead,
    LocalDateTime createdAt) {

}
