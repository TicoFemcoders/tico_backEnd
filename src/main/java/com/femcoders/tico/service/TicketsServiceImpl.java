package com.femcoders.tico.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.femcoders.tico.dto.request.TicketCreateReqDTO;
import com.femcoders.tico.dto.response.TicketsResponseDTO;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Tickets;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.TicketsMapper;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.TicketsRepository;
import com.femcoders.tico.repository.UserRepository;

@Service
public class TicketsServiceImpl implements TicketsService {

    @Autowired
    private TicketsRepository ticketsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TicketsMapper ticketsMapper;

    @Override
    public TicketsResponseDTO createTicket(TicketCreateReqDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        Tickets ticket = ticketsMapper.toEntity(dto);
        ticket.setCreatedBy(user);

        Tickets saved = ticketsRepository.save(ticket);
        return ticketsMapper.toResponseDTO(saved);
    }

    @Override
    public List<TicketsResponseDTO> getAllTickets() {
        return ticketsRepository.findAll()
                .stream()
                .map(ticketsMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<TicketsResponseDTO> getTicketsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        return ticketsRepository.findByCreatedById(userId)
                .stream()
                .map(ticketsMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<TicketsResponseDTO> getTicketsByAdmin(Long adminId) {
        return ticketsRepository.findByAssignedToIdAndStatusNot(adminId, TicketStatus.CLOSED)
                .stream()
                .map(ticketsMapper::toResponseDTO)
                .toList();
    }

    @Override
    public TicketsResponseDTO assignAdmin(Long ticketId, Long adminId) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", adminId));

        ticket.setAssignedTo(admin);
        return ticketsMapper.toResponseDTO(ticketsRepository.save(ticket));
    }

    @Override
    public TicketsResponseDTO assignLabel(Long ticketId, Long labelId) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", labelId));

        ticket.getLabels().add(label);
        return ticketsMapper.toResponseDTO(ticketsRepository.save(ticket));
    }

    @Override
    public TicketsResponseDTO removeLabel(Long ticketId, Long labelId) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", labelId));

        ticket.getLabels().remove(label);
        return ticketsMapper.toResponseDTO(ticketsRepository.save(ticket));
    }

    @Override
    public TicketsResponseDTO changePriority(Long ticketId, TicketPriority priority) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        ticket.setPriority(priority);
        return ticketsMapper.toResponseDTO(ticketsRepository.save(ticket));
    }

    @Override
    public TicketsResponseDTO closeTicket(Long ticketId) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        ticket.close();
        return ticketsMapper.toResponseDTO(ticketsRepository.save(ticket));
    }
}