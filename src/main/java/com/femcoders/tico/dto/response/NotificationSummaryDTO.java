package com.femcoders.tico.dto.response;

import java.util.List;

public record NotificationSummaryDTO(
    long totalUnread, 
    List<NotificationResponseDTO> content
) 
{}