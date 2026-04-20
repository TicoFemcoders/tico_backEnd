package com.femcoders.tico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.enums.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedById(Long userId);

    List<Ticket> findByAssignedToId(Long adminId);

    List<Ticket> findByAssignedToIdAndStatus(Long adminId, TicketStatus status);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByLabelsId(Long labelId);

    List<Ticket> findByAssignedToIdAndStatusNot(Long adminId, TicketStatus status);

}