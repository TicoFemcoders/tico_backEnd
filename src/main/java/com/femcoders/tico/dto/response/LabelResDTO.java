package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;

public record LabelResDTO(
        Long id,
        String name,
        String color,
        LocalDateTime createdAt,
        Boolean active,
        Long activeTickets,
        Long closedTickets) {

}
