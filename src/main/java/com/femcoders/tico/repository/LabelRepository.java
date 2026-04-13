package com.femcoders.tico.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.femcoders.tico.entity.Label;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByTicketsId(Long id);
}