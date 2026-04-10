package com.femcoders.tico.service;

import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.repository.TicketMessageRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;

    public TicketMessageService(TicketMessageRepository ticketMessageRepository) {
        this.ticketMessageRepository = ticketMessageRepository;
    }

    public List<TicketMessage> getMessagesByTicketId(UUID ticketId) {
        return ticketMessageRepository.findByTicketId(ticketId);
    }

    public TicketMessage creatMessage(TicketMessage message) {
        return ticketMessageRepository.save(message);
    }

    public void deleteMessage(UUID id) {
        ticketMessageRepository.deleteById(id);
    }

}
