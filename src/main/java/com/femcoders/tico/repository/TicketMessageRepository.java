package com.femcoders.tico.repository;

import com.femcoders.tico.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, UUID> {

    List<TicketMessage> findByTicketId(UUID ticketId);
    
}
