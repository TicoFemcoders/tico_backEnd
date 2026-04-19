package com.femcoders.tico.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.femcoders.tico.dto.response.NotificationResponseDTO;
import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.repository.TicketMessageRepository;

public class NotificationServiceImpl implements NotificationService {

  @Autowired
  private TicketMessageRepository ticketMessageRepository;

  @Autowired
  private AuthService authService;

  @Override
  public void create(Long ticketId, Long authorId, Long recipientId, String content) {
    TicketMessage notification = new TicketMessage();
    notification.setTicketId(ticketId);
    notification.setAuthorId(authorId);
    notification.setRecipientId(recipientId);
    notification.setContent(content);
    notification.setIsInternal(false);
    ticketMessageRepository.save(notification);
  }

  @Override
  public List<NotificationResponseDTO> getUnread() {
    Long userId = authService.getAuthenticatedUser().getId();
    return ticketMessageRepository
        .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId)
        .stream()
        .map(this::toDTO)
        .toList();
  }

  @Override
  public List<NotificationResponseDTO> getAll() {
    Long userId = authService.getAuthenticatedUser().getId();
    return ticketMessageRepository
        .findByRecipientIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(this::toDTO)
        .toList();
  }

  @Override
  public void markAsRead(Long notificationId) {
    TicketMessage notification = ticketMessageRepository.findById(notificationId)
        .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", notificationId));
    notification.setIsRead(true);
    ticketMessageRepository.save(notification);
  }

  @Override
  public void markAllAsRead() {
    Long userId = authService.getAuthenticatedUser().getId();
    List<TicketMessage> unread = ticketMessageRepository
        .findByRecipientIdAndIsReadFalse(userId);
    unread.forEach(n -> n.setIsRead(true));
    ticketMessageRepository.saveAll(unread);
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