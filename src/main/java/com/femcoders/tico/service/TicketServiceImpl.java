package com.femcoders.tico.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.femcoders.tico.dto.request.TicketCreateReqDTO;
import com.femcoders.tico.dto.response.TicketResponseDTO;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.event.TicketCreatedEvent;
import com.femcoders.tico.event.TicketEmailEvent;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.TicketMapper;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.repository.UserRepository;
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
        public TicketResponseDTO createTicket(TicketCreateReqDTO dto) {
                User user = authService.getAuthenticatedUser();

                Ticket ticket = ticketMapper.toEntity(dto);
                ticket.setCreatedBy(user);

                Ticket saved = ticketsRepository.save(ticket);

                eventPublisher.publishEvent(new TicketCreatedEvent(saved));

                notificationService.create(
                                saved.getId(),
                                user,
                                user.getId(),
                                "Tu ticket ha sido creado: " + saved.getEmailSubject());

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TicketResponseDTO> getAllTickets(Pageable pageable) {
                return ticketsRepository.findAll(pageable)
                                .map(ticketMapper::toResponseDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TicketResponseDTO> getTicketsByUser(Pageable pageable) {
                User user = authService.getAuthenticatedUser();
                return ticketsRepository.findByCreatedById(user.getId(), pageable)
                                .map(ticketMapper::toResponseDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<TicketResponseDTO> getTicketsByAdmin(Pageable pageable) {
                User admin = authService.getAuthenticatedUser();
                return ticketsRepository.findByAssignedToIdAndStatusNot(admin.getId(), TicketStatus.CLOSED, pageable)
                                .map(ticketMapper::toResponseDTO);
        }

        @Override
        @Transactional
        public TicketResponseDTO assignAdmin(Long ticketId, Long adminId) {
                Ticket ticket = ticketsRepository.findById(ticketId)
                                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

                User admin = userRepository.findById(adminId)
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", adminId));

                User currentUser = authService.getAuthenticatedUser();
                boolean isReassignment = ticket.getAssignedTo() != null;
                String content = isReassignment
                                ? "Te han reasignado el ticket: " + ticket.getEmailSubject()
                                : "Se te ha asignado un nuevo ticket: " + ticket.getEmailSubject();

                ticket.setAssignedTo(admin);
                Ticket saved = ticketsRepository.save(ticket);

                notificationService.create(
                                ticket.getId(),
                                currentUser,
                                admin.getId(),
                                content);

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public TicketResponseDTO assignLabel(Long ticketId, Long labelId) {
                Ticket ticket = ticketsRepository.findById(ticketId)
                                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

                Label label = labelRepository.findById(labelId)
                                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", labelId));

                ticket.getLabels().add(label);
                return ticketMapper.toResponseDTO(ticketsRepository.save(ticket));
        }

        @Override
        @Transactional
        public TicketResponseDTO removeLabel(Long ticketId, Long labelId) {
                Ticket ticket = ticketsRepository.findById(ticketId)
                                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

                Label label = labelRepository.findById(labelId)
                                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", labelId));

                ticket.getLabels().remove(label);
                return ticketMapper.toResponseDTO(ticketsRepository.save(ticket));
        }

        @Override
        @Transactional(readOnly = true)
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
        @Transactional
        public TicketResponseDTO changePriority(Long ticketId, TicketPriority priority) {
                Ticket ticket = loadTicketForAssignedAdmin(ticketId);
                User currentUser = authService.getAuthenticatedUser();

                ticket.setPriority(priority);
                Ticket saved = ticketsRepository.save(ticket);

                eventPublisher.publishEvent(new TicketEmailEvent(
                                "PRIORITY_CHANGED",
                                ticket.getCreatedBy().getEmail(),
                                ticket.getCreatedBy().getName(),
                                ticket.getEmailSubject(),
                                TicketStatusHelper.priorityToSpanish(priority)));

                notificationService.create(
                                ticket.getId(),
                                currentUser,
                                ticket.getCreatedBy().getId(),
                                "Prioridad actualizada a " + TicketStatusHelper.priorityToSpanish(priority) + ": "
                                                + ticket.getEmailSubject());

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public TicketResponseDTO changeStatus(Long ticketId, TicketStatus status) {
                Ticket ticket = loadTicketForAssignedAdmin(ticketId);
                User currentUser = authService.getAuthenticatedUser();

                if (status == TicketStatus.CLOSED) {
                        ticket.close();
                        Ticket saved = ticketsRepository.save(ticket);

                        eventPublisher.publishEvent(new TicketEmailEvent(
                                        "CLOSED",
                                        ticket.getCreatedBy().getEmail(),
                                        ticket.getCreatedBy().getName(),
                                        ticket.getEmailSubject(),
                                        null));

                        notificationService.create(
                                        ticket.getId(),
                                        currentUser,
                                        ticket.getCreatedBy().getId(),
                                        "Tu ticket ha sido cerrado: " + ticket.getEmailSubject());

                        return ticketMapper.toResponseDTO(saved);
                }

                ticket.setStatus(status);
                Ticket saved = ticketsRepository.save(ticket);

                eventPublisher.publishEvent(new TicketEmailEvent(
                                "STATUS_CHANGED",
                                ticket.getCreatedBy().getEmail(),
                                ticket.getCreatedBy().getName(),
                                ticket.getEmailSubject(),
                                TicketStatusHelper.statusToSpanish(status)));

                notificationService.create(
                                ticket.getId(),
                                currentUser,
                                ticket.getCreatedBy().getId(),
                                "Estado actualizado a " + TicketStatusHelper.statusToSpanish(status) + ": "
                                                + ticket.getEmailSubject());

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public TicketResponseDTO closeTicket(Long ticketId, String closingMessage) {
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

                eventPublisher.publishEvent(new TicketEmailEvent(
                                "CLOSED",
                                ticket.getCreatedBy().getEmail(),
                                ticket.getCreatedBy().getName(),
                                ticket.getEmailSubject(),
                                null));

                notificationService.create(
                                ticket.getId(),
                                currentUser,
                                ticket.getCreatedBy().getId(),
                                "Tu ticket ha sido cerrado: " + ticket.getEmailSubject());

                return ticketMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
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
                        throw new BadRequestException(
                                        "Solo el Admin asignado o el creador del ticket pueden reactivarlo");
                }

                ticket.setStatus(TicketStatus.OPEN);
                ticket.setClosedAt(null);
                Ticket saved = ticketsRepository.save(ticket);

                if (isAssignedAdmin) {
                        eventPublisher.publishEvent(new TicketEmailEvent(
                                        "REOPENED",
                                        ticket.getCreatedBy().getEmail(),
                                        ticket.getCreatedBy().getName(),
                                        ticket.getEmailSubject(),
                                        null));

                        notificationService.create(
                                        ticket.getId(),
                                        currentUser,
                                        ticket.getCreatedBy().getId(),
                                        "Tu ticket ha sido reactivado: " + ticket.getEmailSubject());
                }

                if (isCreator && ticket.getAssignedTo() != null) {
                        notificationService.create(
                                        ticket.getId(),
                                        currentUser,
                                        ticket.getAssignedTo().getId(),
                                        "El empleado ha reactivado el ticket: " + ticket.getEmailSubject());
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
