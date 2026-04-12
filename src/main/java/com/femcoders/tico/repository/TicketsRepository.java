package com.femcoders.tico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.femcoders.tico.entity.Tickets;
import com.femcoders.tico.enums.TicketStatus;

@Repository
public interface TicketsRepository extends JpaRepository<Tickets, Long> {

    List<Tickets> findByCreatedById(Long userId);

    List<Tickets> findByAssignedToId(Long adminId);

    List<Tickets> findByAssignedToIdAndStatus(Long adminId, TicketStatus status);

    List<Tickets> findByStatus(TicketStatus status);

    List<Tickets> findByLabelsId(Long labelId);
}