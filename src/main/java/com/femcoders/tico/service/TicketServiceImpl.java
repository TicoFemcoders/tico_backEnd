package com.femcoders.tico.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.femcoders.tico.dto.request.TicketCreateRequest;
import com.femcoders.tico.dto.response.TicketResponse;
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
import com.femcoders.tico.service.event.TicketCreatedEvent;
import com.femcoders.tico.service.event.TicketEmailEvent;
import com.femcoders.tico.utils.TicketStatusHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

        private final TicketRepository ticketsRepository;
        private final UserRepository userRepository;
        private final LabelRepository labelRepository;
        private final TicketMapper ticketMapper;
        private final AuthService authService;
        private final ApplicationEventPublisher eventPublisher;
        private final NotificationService notificationService;

        @Override
        @Transactional
        public TicketResponse createTicket(TicketCreateRequest dto) {
                User user = authService.getAuthenticatedUser();

                Ticket ticket = ticketMapper.toEntity(dto);
                ticket.setCreatedBy(user);

                Ticket saved = ticketsRepository.save(ticket);

                if (!user.getRoles().contains(UserRole.ADMIN)) {
                        eventPublisher.publishEvent(new TicketCreatedEvent(saved));
                        notificationService.create(
                                        saved.getId(),
                                        user,
                                        user.getId(),
                                        "Tu ticket ha sido creado: " + saved.getEmailSubject());
                }

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TicketResponse> getAllTickets(Pageable pageable) {
                return ticketsRepository.findAll(pageable)
                                .map(ticketMapper::toResponseDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TicketResponse> getTicketsByUser(Pageable pageable) {
                User user = authService.getAuthenticatedUser();
                return ticketsRepository.findByCreatedById(user.getId(), pageable)
                                .map(ticketMapper::toResponseDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TicketResponse> getTicketsByAdmin(Pageable pageable) {
                User admin = authService.getAuthenticatedUser();
                return ticketsRepository.findByAssignedToId(admin.getId(), pageable)
                                .map(ticketMapper::toResponseDTO);
        }

        @Override
        @Transactional
        public TicketResponse assignAdmin(Long ticketId, Long adminId) {
                Ticket ticket = ticketsRepository.findById(ticketId)
                                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

                User admin = userRepository.findById(adminId)
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", adminId));

                if (!Boolean.TRUE.equals(admin.getIsActive())) {
                        throw new BadRequestException("No se puede asignar el ticket a un administrador inactivo");
                }

                User currentUser = authService.getAuthenticatedUser();
                boolean isReassignment = ticket.getAssignedTo() != null;
                String content = isReassignment
                                ? "Te han reasignado el ticket: " + ticket.getEmailSubject()
                                : "Se te ha asignado un nuevo ticket: " + ticket.getEmailSubject();

                ticket.setAssignedTo(admin);
                Ticket saved = ticketsRepository.save(ticket);

                if (!currentUser.getId().equals(admin.getId())) {
                        notificationService.create(
                                        ticket.getId(),
                                        currentUser,
                                        admin.getId(),
                                        content);
                }

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public TicketResponse assignLabel(Long ticketId, Long labelId) {
                Ticket ticket = ticketsRepository.findById(ticketId)
                                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

                Label label = labelRepository.findById(labelId)
                                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", labelId));

                ticket.getLabels().add(label);
                return ticketMapper.toResponseDTO(ticketsRepository.save(ticket));
        }

        @Override
        @Transactional
        public TicketResponse removeLabel(Long ticketId, Long labelId) {
                Ticket ticket = ticketsRepository.findById(ticketId)
                                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

                Label label = labelRepository.findById(labelId)
                                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", labelId));

                ticket.getLabels().remove(label);
                return ticketMapper.toResponseDTO(ticketsRepository.save(ticket));
        }

        @Override
        @Transactional(readOnly = true)
        public TicketResponse getTicketById(Long ticketId) {
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
        @Transactional
        public TicketResponse changePriority(Long ticketId, TicketPriority priority) {
                Ticket ticket = loadTicketForAssignedAdmin(ticketId);
                User currentUser = authService.getAuthenticatedUser();

                ticket.setPriority(priority);
                Ticket saved = ticketsRepository.save(ticket);

                boolean creatorIsAdmin = ticket.getCreatedBy().getRoles().contains(UserRole.ADMIN);
                boolean creatorDiffFromCurrent = !ticket.getCreatedBy().getId().equals(currentUser.getId());

                if (!creatorIsAdmin) {
                        eventPublisher.publishEvent(new TicketEmailEvent(
                                        "PRIORITY_CHANGED",
                                        ticket.getCreatedBy().getEmail(),
                                        ticket.getCreatedBy().getName(),
                                        ticket.getEmailSubject(),
                                        TicketStatusHelper.priorityToSpanish(priority)));
                }
                if (creatorDiffFromCurrent) {
                        notificationService.create(
                                        ticket.getId(),
                                        currentUser,
                                        ticket.getCreatedBy().getId(),
                                        "Prioridad actualizada a " + TicketStatusHelper.priorityToSpanish(priority)
                                                        + ": "
                                                        + ticket.getEmailSubject());
                }

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public TicketResponse changeStatus(Long ticketId, TicketStatus status) {
                Ticket ticket = loadTicketForAssignedAdmin(ticketId);
                User currentUser = authService.getAuthenticatedUser();

                boolean creatorIsAdmin = ticket.getCreatedBy().getRoles().contains(UserRole.ADMIN);
                boolean creatorDiffFromCurrent = !ticket.getCreatedBy().getId().equals(currentUser.getId());

                if (status == TicketStatus.CLOSED) {
                        ticket.close();
                        Ticket saved = ticketsRepository.save(ticket);

                        if (!creatorIsAdmin) {
                                eventPublisher.publishEvent(new TicketEmailEvent(
                                                "CLOSED",
                                                ticket.getCreatedBy().getEmail(),
                                                ticket.getCreatedBy().getName(),
                                                ticket.getEmailSubject(),
                                                null));
                        }
                        if (creatorDiffFromCurrent) {
                                notificationService.create(
                                                ticket.getId(),
                                                currentUser,
                                                ticket.getCreatedBy().getId(),
                                                "Tu ticket ha sido cerrado: " + ticket.getEmailSubject());
                        }

                        return ticketMapper.toResponseDTO(saved);
                }

                ticket.setStatus(status);
                Ticket saved = ticketsRepository.save(ticket);

                if (!creatorIsAdmin) {
                        eventPublisher.publishEvent(new TicketEmailEvent(
                                        "STATUS_CHANGED",
                                        ticket.getCreatedBy().getEmail(),
                                        ticket.getCreatedBy().getName(),
                                        ticket.getEmailSubject(),
                                        TicketStatusHelper.statusToSpanish(status)));
                }
                if (creatorDiffFromCurrent) {
                        notificationService.create(
                                        ticket.getId(),
                                        currentUser,
                                        ticket.getCreatedBy().getId(),
                                        "Estado actualizado a " + TicketStatusHelper.statusToSpanish(status) + ": "
                                                        + ticket.getEmailSubject());
                }

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public TicketResponse closeTicket(Long ticketId, String closingMessage) {
                Ticket ticket = loadTicketForAssignedAdmin(ticketId);
                if (ticket.getStatus() == TicketStatus.CLOSED) {
                        return ticketMapper.toResponseDTO(ticket);
                }
                User currentUser = authService.getAuthenticatedUser();

                ticket.close();
                if (closingMessage != null && !closingMessage.isBlank()) {
                        ticket.setClosingMessage(closingMessage);
                }
                Ticket saved = ticketsRepository.save(ticket);

                boolean creatorIsAdmin = ticket.getCreatedBy().getRoles().contains(UserRole.ADMIN);
                boolean creatorDiffFromCurrent = !ticket.getCreatedBy().getId().equals(currentUser.getId());

                if (!creatorIsAdmin) {
                        eventPublisher.publishEvent(new TicketEmailEvent(
                                        "CLOSED",
                                        ticket.getCreatedBy().getEmail(),
                                        ticket.getCreatedBy().getName(),
                                        ticket.getEmailSubject(),
                                        null));
                }
                if (creatorDiffFromCurrent) {
                        notificationService.create(
                                        ticket.getId(),
                                        currentUser,
                                        ticket.getCreatedBy().getId(),
                                        "Tu ticket ha sido cerrado: " + ticket.getEmailSubject());
                }

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public TicketResponse reopenTicket(Long ticketId) {
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
                        throw new BadRequestException(
                                        "Solo el Admin asignado o el creador del ticket pueden reactivarlo");
                }

                boolean creatorIsAdmin = ticket.getCreatedBy().getRoles().contains(UserRole.ADMIN);

                ticket.setStatus(TicketStatus.OPEN);
                ticket.setClosedAt(null);
                ticket.getLabels().removeIf(label -> !Boolean.TRUE.equals(label.getIsActive()));
                Ticket saved = ticketsRepository.save(ticket);

                if (isAssignedAdmin && !isCreator) {
                        if (!creatorIsAdmin) {
                                eventPublisher.publishEvent(new TicketEmailEvent(
                                                "REOPENED",
                                                ticket.getCreatedBy().getEmail(),
                                                ticket.getCreatedBy().getName(),
                                                ticket.getEmailSubject(),
                                                null));
                        }
                        notificationService.create(
                                        ticket.getId(),
                                        currentUser,
                                        ticket.getCreatedBy().getId(),
                                        "Tu ticket ha sido reactivado: " + ticket.getEmailSubject());
                }

                if (isCreator && !isAssignedAdmin) {
                        if (!creatorIsAdmin) {
                                eventPublisher.publishEvent(new TicketEmailEvent(
                                                "REOPENED",
                                                ticket.getCreatedBy().getEmail(),
                                                ticket.getCreatedBy().getName(),
                                                ticket.getEmailSubject(),
                                                null));
                        }
                        if (ticket.getAssignedTo() != null) {
                                notificationService.create(
                                                ticket.getId(),
                                                currentUser,
                                                ticket.getAssignedTo().getId(),
                                                "El ticket ha sido reactivado: " + ticket.getEmailSubject());
                        }
                }

                return ticketMapper.toResponseDTO(saved);
        }

        private Ticket loadTicketForAssignedAdmin(Long ticketId) {
                Ticket ticket = ticketsRepository.findById(ticketId)
                                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
                User currentUser = authService.getAuthenticatedUser();
                if (ticket.getAssignedTo() != null
                                && !ticket.getAssignedTo().getId().equals(currentUser.getId())) {
                        throw new BadRequestException("Solo el admin asignado puede modificar este ticket");
                }
                return ticket;
        }
}
