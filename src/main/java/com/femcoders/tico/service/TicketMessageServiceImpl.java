package com.femcoders.tico.service;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

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

@Service
@RequiredArgsConstructor
public class TicketMessageServiceImpl implements TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;
    private final TicketMessageMapper ticketMessageMapper;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;
    private final AuthService authService;
    private final NotificationService notificationService;

    @Override
    public List<TicketMessageResponseDTO> getMessagesByTicketId(Long ticketId) {
        User currentUser = authService.getAuthenticatedUser();
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        if (currentUser.getRoles().contains(UserRole.EMPLOYEE)
                && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No tienes acceso a los mensajes de este ticket");
        }
        return ticketMessageRepository.findByTicketId(ticketId)
                .stream()
                .map(ticketMessageMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TicketMessageResponseDTO createMessage(Long ticketId, TicketMessageRequestDTO dto) {
        User currentUser = authService.getAuthenticatedUser();
        Ticket ticket = loadTicketForAuthorizedUser(ticketId, currentUser);

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

    private Ticket loadTicketForAuthorizedUser(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        if (currentUser.getRoles().contains(UserRole.ADMIN)
                && ticket.getAssignedTo() != null
                && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Solo el admin asignado puede escribir en este ticket");
        }
        return ticket;
    }

}
