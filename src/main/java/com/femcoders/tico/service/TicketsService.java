package com.femcoders.tico.service;

import java.util.List;

import com.femcoders.tico.dto.request.TicketCreateReqDTO;
import com.femcoders.tico.dto.response.TicketsResponseDTO;
import com.femcoders.tico.enums.TicketPriority;

public interface TicketsService {

    TicketsResponseDTO createTicket(TicketCreateReqDTO dto);

    List<TicketsResponseDTO> getAllTickets();

    List<TicketsResponseDTO> getTicketsByUser();

    List<TicketsResponseDTO> getTicketsByAdmin();

    TicketsResponseDTO assignAdmin(Long ticketId, Long adminId);

    TicketsResponseDTO assignLabel(Long ticketId, Long labelId);

    TicketsResponseDTO removeLabel(Long ticketId, Long labelId);

    TicketsResponseDTO changePriority(Long ticketId, TicketPriority priority);

    TicketsResponseDTO closeTicket(Long ticketId);
}