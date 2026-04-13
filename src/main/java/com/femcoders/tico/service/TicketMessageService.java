package com.femcoders.tico.service;

import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.repository.TicketMessageRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.femcoders.tico.dto.TicketMessageRequestDTO;
import com.femcoders.tico.dto.TicketMessageResponseDTO;
import com.femcoders.tico.mapper.TicketMessageMapper;

@Service
public class TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;
    private final TicketMessageMapper ticketMessageMapper;

    public TicketMessageService(TicketMessageRepository ticketMessageRepository) {
        this.ticketMessageRepository = ticketMessageRepository;
        this.ticketMessageMapper = ticketMessageMapper;
    }

    public List<TicketMessageResponseDTO> getMessagesByTicketId(Long ticketId) {
    return ticketMessageRepository.findByTicketId(ticketId)
            .stream()
            .map(ticketMessageMapper::toResponseDTO)
            .collect(Collectors.toList());
}

public TicketMessageResponseDTO createMessage(Long ticketId, TicketMessageRequestDTO dto) {
    TicketMessage message = ticketMessageMapper.toEntity(dto);
    message.setTicketId(ticketId);
    TicketMessage saved = ticketMessageRepository.save(message);
    return ticketMessageMapper.toResponseDTO(saved);
}

public void deleteMessage(Long id) {
    ticketMessageRepository.deleteById(id);
}
}