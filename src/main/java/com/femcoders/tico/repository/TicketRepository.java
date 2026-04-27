package com.femcoders.tico.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.enums.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

        @EntityGraph(attributePaths = { "labels", "createdBy", "assignedTo" })
        public List<Ticket> findAll();

        @EntityGraph(attributePaths = { "labels", "createdBy", "assignedTo" })
        public List<Ticket> findByCreatedById(Long userId);

        public Page<Ticket> findByCreatedById(Long userId, Pageable pageable);

        public List<Ticket> findByAssignedToId(Long adminId);

        public List<Ticket> findByAssignedToIdAndStatus(Long adminId, TicketStatus status);

        public List<Ticket> findByStatus(TicketStatus status);

        public List<Ticket> findByLabelsId(Long labelId);

        @EntityGraph(attributePaths = { "labels", "createdBy", "assignedTo" })
        public List<Ticket> findByAssignedToIdAndStatusNot(Long adminId, TicketStatus status);

        @EntityGraph(attributePaths = { "labels", "createdBy", "assignedTo" })
        public List<Ticket> findByCreatedByIdAndStatusNot(Long userId, TicketStatus status);

        public Page<Ticket> findByAssignedToId(Long adminId, Pageable pageable);

        public Page<Ticket> findByAssignedToIdAndStatusNot(Long adminId, TicketStatus status, Pageable pageable);

        public Page<Ticket> findByAssignedToIdAndStatus(Long adminId, TicketStatus status, Pageable pageable);

        @Query("SELECT t.createdBy.id, COUNT(t) FROM Ticket t " +
                        "WHERE t.status <> com.femcoders.tico.enums.TicketStatus.CLOSED " +
                        "GROUP BY t.createdBy.id")
        public List<Object[]> countOpenTicketsPerUser();

        @Query("SELECT l.id, t.status, COUNT(t) FROM Ticket t JOIN t.labels l GROUP BY l.id, t.status")
        public List<Object[]> countTicketsGroupedByLabelAndStatus();

        @Modifying
        @Transactional
        @Query("UPDATE Ticket t SET t.assignedTo = null " +
                        "WHERE t.assignedTo.id = :adminId " +
                        "AND t.status <> com.femcoders.tico.enums.TicketStatus.CLOSED")
        public int unassignOpenTicketsByAdmin(@Param("adminId") Long adminId);

        @Query("SELECT t.assignedTo.id, COUNT(t) FROM Ticket t " +
                        "WHERE t.assignedTo IS NOT NULL " +
                        "AND t.status <> com.femcoders.tico.enums.TicketStatus.CLOSED " +
                        "GROUP BY t.assignedTo.id")
        public List<Object[]> countOpenTicketsPerAdmin();

}