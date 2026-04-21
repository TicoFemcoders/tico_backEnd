package com.femcoders.tico.service;

import java.util.List;

import com.femcoders.tico.dto.request.TicketCreateReqDTO;
import com.femcoders.tico.dto.response.TicketResponseDTO;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;

public interface TicketService {

    public TicketResponseDTO createTicket(TicketCreateReqDTO dto);

    public List<TicketResponseDTO> getAllTickets();

    public List<TicketResponseDTO> getTicketsByUser();

    public List<TicketResponseDTO> getTicketsByAdmin();

    public TicketResponseDTO assignAdmin(Long ticketId, Long adminId);

    public TicketResponseDTO assignLabel(Long ticketId, Long labelId);

    public TicketResponseDTO removeLabel(Long ticketId, Long labelId);

    public TicketResponseDTO changePriority(Long ticketId, TicketPriority priority);

    public TicketResponseDTO closeTicket(Long ticketId, String closingMessage);

    public TicketResponseDTO getTicketById(Long ticketId);

    public TicketResponseDTO changeStatus(Long ticketId, TicketStatus status);

    public TicketResponseDTO reopenTicket(Long ticketId);
}