package com.femcoders.tico.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.femcoders.tico.dto.request.TicketCreateRequest;
import com.femcoders.tico.dto.response.TicketResponse;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;

public interface TicketService {

    public TicketResponse createTicket(TicketCreateRequest dto);

    public Page<TicketResponse> getAllTickets(Pageable pageable);

    public Page<TicketResponse> getTicketsByUser(Pageable pageable);

    public Page<TicketResponse> getTicketsByAdmin(Pageable pageable);

    public TicketResponse assignAdmin(Long ticketId, Long adminId);

    public TicketResponse assignLabel(Long ticketId, Long labelId);

    public TicketResponse removeLabel(Long ticketId, Long labelId);

    public TicketResponse changePriority(Long ticketId, TicketPriority priority);

    public TicketResponse closeTicket(Long ticketId, String closingMessage);

    public TicketResponse getTicketById(Long ticketId);

    public TicketResponse changeStatus(Long ticketId, TicketStatus status);

    public TicketResponse reopenTicket(Long ticketId);
}