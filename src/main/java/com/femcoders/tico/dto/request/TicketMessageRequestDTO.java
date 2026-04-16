package com.femcoders.tico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketMessageRequestDTO(
    @NotNull 
    Long authorId,
    
    @NotNull 
    Long ticketId,
    
    @NotBlank 
    String content,
    
    @NotNull 
    Boolean isInternal
) {}