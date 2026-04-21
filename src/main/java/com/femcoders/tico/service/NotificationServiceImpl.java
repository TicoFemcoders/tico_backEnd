package com.femcoders.tico.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.femcoders.tico.dto.response.NotificationResponseDTO;
import com.femcoders.tico.dto.response.NotificationSummaryDTO;
import com.femcoders.tico.entity.TicketMessage;

@Service
public class NotificationServiceImpl implements NotificationService {

  @Autowired
  private TicketMessageService ticketMessageService;

  @Autowired
  private AuthService authService;

  @Override
  public void create(Long ticketId, Long authorId, Long recipientId, String content) {
    ticketMessageService.createNotification(ticketId, authorId, recipientId, content);
  }

  @Override
  public List<NotificationResponseDTO> getUnread() {
    Long userId = authService.getAuthenticatedUser().getId();
    return ticketMessageService.findUnreadByRecipient(userId)
        .stream()
        .map(this::toDTO)
        .toList();
  }

  @Override
  public List<NotificationResponseDTO> getAll() {
    Long userId = authService.getAuthenticatedUser().getId();
    return ticketMessageService.findAllByRecipient(userId)
        .stream()
        .map(this::toDTO)
        .toList();
  }

  @Override
  public NotificationSummaryDTO getPaginatedSummary(int page, int size) {
    Long userId = authService.getAuthenticatedUser().getId();
    long unreadCount = ticketMessageService.countUnreadByRecipient(userId);
    Pageable pageable = PageRequest.of(page, size);
    List<NotificationResponseDTO> recentNotifications = ticketMessageService
        .findUnreadByRecipientPaginated(userId, pageable)
        .stream()
        .map(this::toDTO)
        .toList();
    return new NotificationSummaryDTO(unreadCount, recentNotifications);
  }

  @Override
  public void markAsRead(Long notificationId) {
    TicketMessage notification = ticketMessageService.findNotificationById(notificationId);
    notification.setIsRead(true);
    ticketMessageService.saveNotification(notification);
  }

  @Override
  public void markAllAsRead() {
    Long userId = authService.getAuthenticatedUser().getId();
    List<TicketMessage> unread = ticketMessageService.findUnreadByRecipient(userId);
    unread.forEach(n -> n.setIsRead(true));
    ticketMessageService.saveAllNotifications(unread);
  }

  private NotificationResponseDTO toDTO(TicketMessage n) {
    return new NotificationResponseDTO(
        n.getId(),
        n.getTicketId(),
        n.getContent(),
        n.getIsRead(),
        n.getCreatedAt());
  }
}
