package com.femcoders.tico.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class TicketMessageRequestDTO {

    private UUID authorId;
    private String content;
    private Boolean isInternal;

}
