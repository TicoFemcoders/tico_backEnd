package com.femcoders.tico.dto;

import java.util.List;

import com.femcoders.tico.dto.response.NotificationResponse;

public record NotificationSummary(
    long totalUnread,
    List<NotificationResponse> content
) {}
