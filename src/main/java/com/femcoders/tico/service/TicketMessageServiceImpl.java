package com.femcoders.tico.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.femcoders.tico.dto.request.TicketMessageRequest;
import com.femcoders.tico.dto.response.TicketMessageResponse;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.TicketMessageMapper;
import com.femcoders.tico.repository.TicketMessageRepository;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.service.event.TicketEmailEvent;

@Service
@RequiredArgsConstructor
public class TicketMessageServiceImpl implements TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;
    private final TicketMessageMapper ticketMessageMapper;
    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthService authService;
    private final NotificationService notificationService;

    @Override
    public Page<TicketMessageResponse> getMessagesByTicketId(Long ticketId, Pageable pageable) {
        User currentUser = authService.getAuthenticatedUser();
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        if (currentUser.getRoles().contains(UserRole.EMPLOYEE)
                && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No tienes acceso a los mensajes de este ticket");
        }
        return ticketMessageRepository.findByTicketIdAndRecipientIdIsNullOrderByCreatedAtDesc(ticketId, pageable)
                .map(ticketMessageMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public TicketMessageResponse createMessage(Long ticketId, TicketMessageRequest dto) {
        User currentUser = authService.getAuthenticatedUser();
        Ticket ticket = loadTicketForAuthorizedUser(ticketId, currentUser);

        TicketMessage message = ticketMessageMapper.toEntity(dto);
        message.setTicketId(ticketId);
        message.setAuthor(currentUser);
        TicketMessage saved = ticketMessageRepository.save(message);

        boolean authorIsAssignedAdmin = ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(currentUser.getId());
        boolean authorIsCreator = ticket.getCreatedBy().getId().equals(currentUser.getId());
        boolean creatorIsAdmin = ticket.getCreatedBy().getRoles().contains(UserRole.ADMIN);

        // Assigned admin sends message (not the creator)
        if (authorIsAssignedAdmin && !authorIsCreator) {
            if (!creatorIsAdmin) {
                eventPublisher.publishEvent(new TicketEmailEvent(
                        "NEW_MESSAGE",
                        ticket.getCreatedBy().getEmail(),
                        ticket.getCreatedBy().getName(),
                        ticket.getEmailSubject(),
                        saved.getContent()));
            }
            notificationService.create(
                    ticket.getId(),
                    currentUser,
                    ticket.getCreatedBy().getId(),
                    "Nueva respuesta en tu ticket: " + ticket.getEmailSubject());
        }

        // Creator sends message (not the assigned admin)
        if (authorIsCreator && !authorIsAssignedAdmin && ticket.getAssignedTo() != null) {
            notificationService.create(
                    ticket.getId(),
                    currentUser,
                    ticket.getAssignedTo().getId(),
                    "Nueva respuesta en el ticket: " + ticket.getEmailSubject());
        }

        return ticketMessageMapper.toResponseDTO(saved);
    }

    @Override
    public void deleteMessage(Long id) {
        ticketMessageRepository.deleteById(id);
    }

    private Ticket loadTicketForAuthorizedUser(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        if (currentUser.getRoles().contains(UserRole.EMPLOYEE)
                && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Solo el creador del ticket puede responder en él");
        }
        if (currentUser.getRoles().contains(UserRole.ADMIN)
                && ticket.getAssignedTo() != null
                && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Solo el admin asignado puede escribir en este ticket");
        }
        return ticket;
    }
}
