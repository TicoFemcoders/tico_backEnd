package com.femcoders.tico.service;

import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.repository.TicketMessageRepository;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;

    public TicketMessageService(TicketMessageRepository ticketMessageRepository) {
        this.ticketMessageRepository = ticketMessageRepository;
    }

   public List<TicketMessage> getMessagesByTicketId(Long ticketId) {
    return ticketMessageRepository.findByTicketId(ticketId);
}

    public TicketMessage createMessage(TicketMessage message) {
        return ticketMessageRepository.save(message);
    }

    public void deleteMessage(Long id) {
        ticketMessageRepository.deleteById(id);
    }

}
