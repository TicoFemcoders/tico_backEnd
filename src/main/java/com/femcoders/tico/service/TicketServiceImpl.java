package com.femcoders.tico.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.femcoders.tico.dto.request.TicketCreateReqDTO;
import com.femcoders.tico.dto.response.TicketResponseDTO;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.TicketMapper;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.repository.UserRepository;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public TicketResponseDTO createTicket(TicketCreateReqDTO dto) {
        User user = authService.getAuthenticatedUser();

        Ticket ticket = ticketMapper.toEntity(dto);
        ticket.setCreatedBy(user);

        Ticket saved = ticketsRepository.save(ticket);

        emailService.sendTicketCreatedEmail(
                user.getEmail(),
                user.getName(),
                saved.getEmailSubject());

        notificationService.create(
                saved.getId(),
                user.getId(),
                user.getId(),
                "Tu ticket ha sido creado: " + saved.getEmailSubject());

        return ticketMapper.toResponseDTO(saved);
    }

    @Override
    public List<TicketResponseDTO> getAllTickets() {
        return ticketsRepository.findAll()
                .stream()
                .map(ticketMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<TicketResponseDTO> getTicketsByUser() {
        User user = authService.getAuthenticatedUser();

        return ticketsRepository.findByCreatedById(user.getId())
                .stream()
                .map(ticketMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<TicketResponseDTO> getTicketsByAdmin() {
        User admin = authService.getAuthenticatedUser();
        return ticketsRepository.findByAssignedToIdAndStatusNot(admin.getId(), TicketStatus.CLOSED)
                .stream()
                .map(ticketMapper::toResponseDTO)
                .toList();
    }

    @Override
    public TicketResponseDTO assignAdmin(Long ticketId, Long adminId) {
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", adminId));

        boolean isReassignment = ticket.getAssignedTo() != null;
        String content = isReassignment
                ? "Te han reasignado el ticket: " + ticket.getEmailSubject()
                : "Se te ha asignado un nuevo ticket: " + ticket.getEmailSubject();

        ticket.setAssignedTo(admin);
        Ticket saved = ticketsRepository.save(ticket);

        notificationService.create(
                ticket.getId(),
                authService.getAuthenticatedUser().getId(),
                admin.getId(),
                content);

        return ticketMapper.toResponseDTO(saved);
    }

    @Override
    public TicketResponseDTO assignLabel(Long ticketId, Long labelId) {
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", labelId));

        ticket.getLabels().add(label);
        return ticketMapper.toResponseDTO(ticketsRepository.save(ticket));
    }

    @Override
    public TicketResponseDTO removeLabel(Long ticketId, Long labelId) {
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", labelId));

        ticket.getLabels().remove(label);
        return ticketMapper.toResponseDTO(ticketsRepository.save(ticket));
    }

    @Override
    public TicketResponseDTO getTicketById(Long ticketId) {
        User currentUser = authService.getAuthenticatedUser();
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        if (currentUser.getRoles().contains(UserRole.EMPLOYEE)) {
            if (!ticket.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Ticket", "id", ticketId);
            }
        }
        return ticketMapper.toResponseDTO(ticket);
    }

    @Override
    public TicketResponseDTO changePriority(Long ticketId, TicketPriority priority) {
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User currentUser = authService.getAuthenticatedUser();
        if (ticket.getAssignedTo() != null
                && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Solo el admin asignado puede modificar este ticket");
        }

        ticket.setPriority(priority);
        Ticket saved = ticketsRepository.save(ticket);

        emailService.sendPriorityChangedEmail(
                ticket.getCreatedBy().getEmail(),
                ticket.getCreatedBy().getName(),
                ticket.getEmailSubject(),
                priorityToSpanish(priority));

        notificationService.create(
                ticket.getId(),
                currentUser.getId(),
                ticket.getCreatedBy().getId(),
                "Prioridad actualizada a " + priorityToSpanish(priority) + ": " + ticket.getEmailSubject());

        return ticketMapper.toResponseDTO(saved);
    }

    @Override
    public TicketResponseDTO changeStatus(Long ticketId, TicketStatus status) {
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User currentUser = authService.getAuthenticatedUser();
        if (ticket.getAssignedTo() != null
                && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Solo el admin asignado puede modificar este ticket");
        }

        if (status == TicketStatus.CLOSED) {
            ticket.close();
            Ticket saved = ticketsRepository.save(ticket);
            emailService.sendTicketClosedEmail(
                    ticket.getCreatedBy().getEmail(),
                    ticket.getCreatedBy().getName(),
                    ticket.getEmailSubject());

            notificationService.create(
                    ticket.getId(),
                    currentUser.getId(),
                    ticket.getCreatedBy().getId(),
                    "Tu ticket ha sido cerrado: " + ticket.getEmailSubject());

            return ticketMapper.toResponseDTO(saved);
        }

        ticket.setStatus(status);
        Ticket saved = ticketsRepository.save(ticket);

        emailService.sendStatusChangedEmail(
                ticket.getCreatedBy().getEmail(),
                ticket.getCreatedBy().getName(),
                ticket.getEmailSubject(),
                statusToSpanish(status));

        notificationService.create(
                ticket.getId(),
                currentUser.getId(),
                ticket.getCreatedBy().getId(),
                "Estado actualizado a " + statusToSpanish(status) + ": " + ticket.getEmailSubject());

        return ticketMapper.toResponseDTO(saved);
    }

    @Override
    public TicketResponseDTO closeTicket(Long ticketId, String closingMessage) {
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User currentUser = authService.getAuthenticatedUser();
        if (ticket.getAssignedTo() != null
                && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Solo el admin asignado puede cerrar este ticket");
        }

        ticket.close();
        if (closingMessage != null && !closingMessage.isBlank()) {
            ticket.setClosingMessage(closingMessage);
        }
        Ticket saved = ticketsRepository.save(ticket);

        emailService.sendTicketClosedEmail(
                ticket.getCreatedBy().getEmail(),
                ticket.getCreatedBy().getName(),
                ticket.getEmailSubject());

        notificationService.create(
                ticket.getId(),
                currentUser.getId(),
                ticket.getCreatedBy().getId(),
                "Tu ticket ha sido cerrado: " + ticket.getEmailSubject());

        return ticketMapper.toResponseDTO(saved);
    }

    @Override
    public TicketResponseDTO reopenTicket(Long ticketId) {
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (ticket.getStatus() != TicketStatus.CLOSED) {
            throw new BadRequestException("El ticket no está cerrado y no puede reactivarse");
        }

        User currentUser = authService.getAuthenticatedUser();

        boolean isAssignedAdmin = ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(currentUser.getId());
        boolean isCreator = ticket.getCreatedBy().getId().equals(currentUser.getId());

        if (!isAssignedAdmin && !isCreator) {
            throw new BadRequestException("Solo el Admin asignado o el creador del ticket pueden reactivarlo");
        }

        ticket.setStatus(TicketStatus.OPEN);
        ticket.setClosedAt(null);
        Ticket saved = ticketsRepository.save(ticket);

        if (isAssignedAdmin) {
            emailService.sendTicketReopenedEmail(
                    ticket.getCreatedBy().getEmail(),
                    ticket.getCreatedBy().getName(),
                    ticket.getEmailSubject());

            notificationService.create(
                    ticket.getId(),
                    currentUser.getId(),
                    ticket.getCreatedBy().getId(),
                    "Tu ticket ha sido reactivado: " + ticket.getEmailSubject());
        }

        if (isCreator) {
            // campana al admin → task_1.19
        }

        return ticketMapper.toResponseDTO(saved);

    }

    private String priorityToSpanish(TicketPriority priority) {
        return switch (priority) {
            case LOW -> "Baja";
            case MEDIUM -> "Media";
            case HIGH -> "Alta";
            case CRITICAL -> "Crítica";
        };
    }

    private String statusToSpanish(TicketStatus status) {
        return switch (status) {
            case OPEN -> "Abierto";
            case IN_PROGRESS -> "En progreso";
            case CLOSED -> "Cerrado";
        };
    }
}
