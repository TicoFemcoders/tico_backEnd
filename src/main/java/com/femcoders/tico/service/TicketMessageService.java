package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.TicketMessageRequestDTO;
import com.femcoders.tico.dto.response.TicketMessageResponseDTO;

import java.util.List;

public interface TicketMessageService {

    public List<TicketMessageResponseDTO> getMessagesByTicketId(Long ticketId);

    public TicketMessageResponseDTO createMessage(Long ticketId, TicketMessageRequestDTO dto);

    public void deleteMessage(Long id);

}