package com.femcoders.tico.dto.response;

import com.femcoders.tico.enums.UserRole;
import java.time.LocalDateTime;

public record UserResponseDTO(
    Long id,
    String name,
    String email,
    UserRole role,
    Boolean isActive,
    LocalDateTime createdAt
) {}