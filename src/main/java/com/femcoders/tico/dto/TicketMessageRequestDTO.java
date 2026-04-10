package com.femcoders.tico.dto;


import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class TicketMessageRequestDTO {


    
    @NotNull
    private UUID authorId;

   @NotBlank
    private String content;

    @NotNull
    private Boolean isInternal;

    

}
