package com.femcoders.tico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketMessageRequestDTO {

    @NotNull
    private Long authorId;

    @NotBlank
    private String content;

    @NotNull
    private Boolean isInternal;
}
