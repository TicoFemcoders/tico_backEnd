package com.femcoders.tico.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.femcoders.tico.entity.Label;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByTicketsId(Long id);

@Query("SELECT COUNT(t) FROM Ticket t JOIN t.labels l WHERE l.id = :labelId AND t.status != 'CLOSED'")
long countActiveTicketsByLabelId(@Param("labelId") Long labelId);

@Query("SELECT COUNT(t) FROM Ticket t JOIN t.labels l WHERE l.id = :labelId AND t.status = 'CLOSED'")
long countClosedTicketsByLabelId(@Param("labelId") Long labelId);
}