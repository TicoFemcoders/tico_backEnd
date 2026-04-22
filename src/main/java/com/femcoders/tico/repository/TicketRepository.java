package com.femcoders.tico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.enums.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @EntityGraph(attributePaths = {"labels", "createdBy", "assignedTo"})
    List<Ticket> findAll();

    @EntityGraph(attributePaths = {"labels", "createdBy", "assignedTo"})
    List<Ticket> findByCreatedById(Long userId);

    List<Ticket> findByAssignedToId(Long adminId);

    List<Ticket> findByAssignedToIdAndStatus(Long adminId, TicketStatus status);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByLabelsId(Long labelId);

    @EntityGraph(attributePaths = {"labels", "createdBy", "assignedTo"})
    List<Ticket> findByAssignedToIdAndStatusNot(Long adminId, TicketStatus status);

    @Query("SELECT t.createdBy.id, COUNT(t) FROM Ticket t " +
            "WHERE t.status <> com.femcoders.tico.enums.TicketStatus.CLOSED " +
            "GROUP BY t.createdBy.id")
    List<Object[]> countOpenTicketsPerUser();

    @Query("SELECT l.id, t.status, COUNT(t) FROM Ticket t JOIN t.labels l GROUP BY l.id, t.status")
    List<Object[]> countTicketsGroupedByLabelAndStatus();

}