package com.femcoders.tico.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignAdminRequestDTO(
        @NotNull Long adminId
) {}
