package com.femcoders.tico.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.femcoders.tico.dto.request.TicketMessageRequestDTO;
import com.femcoders.tico.dto.response.TicketMessageResponseDTO;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.TicketMessageMapper;
import com.femcoders.tico.repository.TicketMessageRepository;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.repository.UserRepository;

@Service
public class TicketMessageServiceImpl implements TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;
    private final TicketMessageMapper ticketMessageMapper;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TicketMessageServiceImpl(
            TicketMessageRepository ticketMessageRepository,
            TicketMessageMapper ticketMessageMapper,
            TicketRepository ticketRepository,
            EmailService emailService,
            AuthService authService,
            UserRepository userRepository,
            @Lazy NotificationService notificationService) {
        this.ticketMessageRepository = ticketMessageRepository;
        this.ticketMessageMapper = ticketMessageMapper;
        this.ticketRepository = ticketRepository;
        this.emailService = emailService;
        this.authService = authService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<TicketMessageResponseDTO> getMessagesByTicketId(Long ticketId) {
        return ticketMessageRepository.findByTicketId(ticketId)
                .stream()
                .map(ticketMessageMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TicketMessageResponseDTO createMessage(Long ticketId, TicketMessageRequestDTO dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User currentUser = authService.getAuthenticatedUser();
        if (currentUser.getRoles().contains(UserRole.ADMIN)
                && ticket.getAssignedTo() != null
                && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Solo el admin asignado puede escribir en la conversación de este ticket");
        }

        TicketMessage message = ticketMessageMapper.toEntity(dto);
        message.setTicketId(ticketId);
        message.setAuthor(currentUser);
        TicketMessage saved = ticketMessageRepository.save(message);

        boolean authorIsAssignedAdmin = ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(currentUser.getId());

        if (authorIsAssignedAdmin) {
            emailService.sendNewMessageEmail(
                    ticket.getCreatedBy().getEmail(),
                    ticket.getCreatedBy().getName(),
                    ticket.getEmailSubject(),
                    saved.getContent());

            notificationService.create(
                    ticket.getId(),
                    currentUser.getId(),
                    ticket.getCreatedBy().getId(),
                    "Nueva respuesta en tu ticket: " + ticket.getEmailSubject());
        }

        boolean authorIsCreator = ticket.getCreatedBy().getId().equals(currentUser.getId());
        if (authorIsCreator && ticket.getAssignedTo() != null) {
            notificationService.create(
                    ticket.getId(),
                    currentUser.getId(),
                    ticket.getAssignedTo().getId(),
                    "Nueva respuesta del empleado en: " + ticket.getEmailSubject());
        }

        return ticketMessageMapper.toResponseDTO(saved);
    }

    @Override
    public void deleteMessage(Long id) {
        ticketMessageRepository.deleteById(id);
    }

    @Override
    public void createNotification(Long ticketId, Long authorId, Long recipientId, String content) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", authorId));
        TicketMessage notification = new TicketMessage();
        notification.setTicketId(ticketId);
        notification.setAuthor(author);
        notification.setRecipientId(recipientId);
        notification.setContent(content);
        ticketMessageRepository.save(notification);
    }

    @Override
    public List<TicketMessage> findUnreadByRecipient(Long userId) {
        return ticketMessageRepository
                .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<TicketMessage> findAllByRecipient(Long userId) {
        return ticketMessageRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long countUnreadByRecipient(Long userId) {
        return ticketMessageRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    public List<TicketMessage> findUnreadByRecipientPaginated(Long userId, Pageable pageable) {
        return ticketMessageRepository
                .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                .getContent();
    }

    @Override
    public TicketMessage findNotificationById(Long id) {
        return ticketMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", id));
    }

    @Override
    public TicketMessage saveNotification(TicketMessage notification) {
        return ticketMessageRepository.save(notification);
    }

    @Override
    public void saveAllNotifications(List<TicketMessage> notifications) {
        ticketMessageRepository.saveAll(notifications);
    }

}
