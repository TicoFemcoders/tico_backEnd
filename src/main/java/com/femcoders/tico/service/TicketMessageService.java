package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.TicketMessageRequestDTO;
import com.femcoders.tico.dto.response.TicketMessageResponseDTO;

import java.util.List;

public interface TicketMessageService {

    List<TicketMessageResponseDTO> getMessagesByTicketId(Long ticketId);

    TicketMessageResponseDTO createMessage(Long ticketId, TicketMessageRequestDTO dto);

    void deleteMessage(Long id);

}