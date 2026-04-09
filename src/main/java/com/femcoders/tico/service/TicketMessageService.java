package com.femcoders.tico.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.repository.TicketMessageRepository;

@Service
public class TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;

    public TicketMessageService(TicketMessageRepository ticketMessageRepository) {
        this.ticketMessageRepository = ticketMessageRepository;
    }

    public List<TicketMessage> getMessagesByTicketId(UUID ticketId) {
        return ticketMessageRepository.findByTicketId(ticketId);
    }

    public TicketMessage createMessage(TicketMessage message) {
        return ticketMessageRepository.save(message);
    }

    public void deleteMessage(UUID id) {
        ticketMessageRepository.deleteById(id);
    }

}
