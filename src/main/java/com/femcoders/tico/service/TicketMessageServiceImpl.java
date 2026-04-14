package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.TicketMessageRequestDTO;
import com.femcoders.tico.dto.response.TicketMessageResponseDTO;
import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.mapper.TicketMessageMapper;
import com.femcoders.tico.repository.TicketMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketMessageServiceImpl implements TicketMessageService {

    @Autowired
    private TicketMessageRepository ticketMessageRepository;

    @Autowired
    private TicketMessageMapper ticketMessageMapper;

    @Override
    public List<TicketMessageResponseDTO> getMessagesByTicketId(Long ticketId) {
        return ticketMessageRepository.findByTicketId(ticketId)
                .stream()
                .map(ticketMessageMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TicketMessageResponseDTO createMessage(Long ticketId, TicketMessageRequestDTO dto) {
        TicketMessage message = ticketMessageMapper.toEntity(dto);
        message.setTicketId(ticketId);
        TicketMessage saved = ticketMessageRepository.save(message);
        return ticketMessageMapper.toResponseDTO(saved);
    }

    @Override
    public void deleteMessage(Long id) {
        ticketMessageRepository.deleteById(id);
    }

}
