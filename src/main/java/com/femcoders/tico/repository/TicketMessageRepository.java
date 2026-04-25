package com.femcoders.tico.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.femcoders.tico.entity.TicketMessage;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    Page<TicketMessage> findByTicketIdAndRecipientIdIsNullOrderByCreatedAtDesc(Long ticketId, Pageable pageable);

    List<TicketMessage> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);

    List<TicketMessage> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    Page<TicketMessage> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Page<TicketMessage> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE TicketMessage m SET m.isRead = true WHERE m.recipientId = :userId AND m.isRead = false")
    int markAllAsReadByRecipient(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE TicketMessage m SET m.isRead = true WHERE m.id = :id AND m.recipientId = :userId AND m.isRead = false")
    int markAsReadByIdAndRecipient(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE TicketMessage m SET m.author = null WHERE m.author.id = :userId")
    void clearAuthorByUserId(@Param("userId") Long userId);
}
