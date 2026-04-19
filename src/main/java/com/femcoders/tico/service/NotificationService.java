package com.femcoders.tico.service;

import java.util.List;

import com.femcoders.tico.dto.response.NotificationResponseDTO;

public interface NotificationService {

  public void create(Long ticketId, Long authorId, Long recipientId, String content);

  public List<NotificationResponseDTO> getUnread();

  public List<NotificationResponseDTO> getAll();

  public void markAsRead(Long notificationId);

  public void markAllAsRead();

}
