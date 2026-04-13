package com.femcoders.tico.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;

import com.femcoders.tico.entity.TicketMessage;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long>  {

    List<TicketMessage> findByTicketId(Long ticketId);

}
