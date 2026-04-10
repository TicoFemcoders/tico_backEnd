package com.femcoders.tico.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class TicketMessageResponseDTO {

    private UUID id;
    private UUID ticketId;
    private UUID authorId;
    private String content;
    private Boolean isInternal;
    private LocalDateTime createdAt;

}
