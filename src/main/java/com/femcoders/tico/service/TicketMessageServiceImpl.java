package com.femcoders.tico.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketMessageServiceImpl implements TicketMessageService {

    @Autowired
    private TicketMessageRepository ticketMessageRepository;

    @Autowired
    private TicketMessageMapper ticketMessageMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthService authService;

    @Autowired
    private NotificationService notificationService;

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

}
