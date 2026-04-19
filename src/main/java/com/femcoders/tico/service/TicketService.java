package com.femcoders.tico.service;

import java.util.List;

import com.femcoders.tico.dto.request.TicketCreateReqDTO;
import com.femcoders.tico.dto.response.TicketResponseDTO;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;

public interface TicketService {

    TicketResponseDTO createTicket(TicketCreateReqDTO dto);

    List<TicketResponseDTO> getAllTickets();

    List<TicketResponseDTO> getTicketsByUser();

    List<TicketResponseDTO> getTicketsByAdmin();

    TicketResponseDTO assignAdmin(Long ticketId, Long adminId);

    TicketResponseDTO assignLabel(Long ticketId, Long labelId);

    TicketResponseDTO removeLabel(Long ticketId, Long labelId);

    TicketResponseDTO changePriority(Long ticketId, TicketPriority priority);

    TicketResponseDTO closeTicket(Long ticketId, String closingMessage);

    TicketResponseDTO getTicketById(Long ticketId);

    TicketResponseDTO changeStatus(Long ticketId, TicketStatus status);
}