package com.femcoders.tico.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TicketMessageResponseDTO {

    private Long id;
    private Long ticketId;
    private Long authorId;
    private String content;
    private Boolean isInternal;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
