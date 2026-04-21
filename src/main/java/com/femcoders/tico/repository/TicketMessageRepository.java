package com.femcoders.tico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.femcoders.tico.entity.TicketMessage;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    List<TicketMessage> findByTicketId(Long ticketId);

    List<TicketMessage> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);

    List<TicketMessage> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<TicketMessage> findByRecipientIdAndIsReadFalse(Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    Page<TicketMessage> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Page<TicketMessage> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
}
