package com.femcoders.tico.dto.response;

import java.util.Set;

public record AuthResponse(
    long userId,
    String name,
    String email,
    Set<String> roles
) {}
