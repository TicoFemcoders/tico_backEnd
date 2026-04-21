package com.femcoders.tico.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.femcoders.tico.dto.request.TicketMessageRequestDTO;
import com.femcoders.tico.dto.response.TicketMessageResponseDTO;
import com.femcoders.tico.entity.TicketMessage;

public interface TicketMessageService {

    public List<TicketMessageResponseDTO> getMessagesByTicketId(Long ticketId);

    public TicketMessageResponseDTO createMessage(Long ticketId, TicketMessageRequestDTO dto);

    public void deleteMessage(Long id);

    public void createNotification(Long ticketId, Long authorId, Long recipientId, String content);

    public List<TicketMessage> findUnreadByRecipient(Long userId);

    public List<TicketMessage> findAllByRecipient(Long userId);

    public long countUnreadByRecipient(Long userId);

    public List<TicketMessage> findUnreadByRecipientPaginated(Long userId, Pageable pageable);

    public TicketMessage findNotificationById(Long id);

    public TicketMessage saveNotification(TicketMessage notification);

    public void saveAllNotifications(List<TicketMessage> notifications);

}