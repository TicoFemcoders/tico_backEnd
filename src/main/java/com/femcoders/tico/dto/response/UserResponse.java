package com.femcoders.tico.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String name,
        String email,
        Set<String> roles,
        Boolean isActive,
        long openTickets,
        LocalDateTime createdAt
        ) {

}
