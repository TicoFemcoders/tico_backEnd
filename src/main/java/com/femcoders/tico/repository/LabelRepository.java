package com.femcoders.tico.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.femcoders.tico.entity.Label;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    List<Label> findByNameContainingIgnoreCase(String name);

    Page<Label> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByTicketsId(Long id);

    List<Label> findByIsActiveTrue();

}