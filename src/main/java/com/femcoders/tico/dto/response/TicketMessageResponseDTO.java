package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TicketMessageResponseDTO {

    private Long id;
    private Long ticketId;
    private Long authorId;
    private String content;
    private Boolean isInternal;
    private LocalDateTime createdAt;

}
