package com.femcoders.tico.dto.response;

import java.util.List;

public record NotificationSummaryResponse(
    long totalUnread,
    List<NotificationResponse> content
) {}
