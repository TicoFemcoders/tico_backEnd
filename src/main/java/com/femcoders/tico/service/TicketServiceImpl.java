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

        ticket.setAssignedTo(admin);
        return ticketMapper.toResponseDTO(ticketsRepository.save(ticket));
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
    public TicketResponseDTO changePriority(Long ticketId, TicketPriority priority) {
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        ticket.setPriority(priority);
        return ticketMapper.toResponseDTO(ticketsRepository.save(ticket));
    }

    @Override
    public TicketResponseDTO closeTicket(Long ticketId, String closingMessage) {
        Ticket ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        ticket.close();
        if (closingMessage != null && !closingMessage.isBlank()) {
            ticket.setClosingMessage(closingMessage);
        }
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
}
