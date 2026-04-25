package com.femcoders.tico.service;

import java.util.List;

import com.femcoders.tico.dto.response.NotificationResponse;
import com.femcoders.tico.dto.response.NotificationSummaryResponse;
import com.femcoders.tico.entity.User;

public interface NotificationService {

  public void create(Long ticketId, User author, Long recipientId, String content);

  public List<NotificationResponse> getUnread();

  public List<NotificationResponse> getAll();

  public NotificationSummaryResponse getPaginatedSummary(int page, int size);

  public void markAsRead(Long notificationId);

  public void markAllAsRead();

}
