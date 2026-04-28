package com.femcoders.tico.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.femcoders.tico.entity.Label;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    public List<Label> findByNameContainingIgnoreCase(String name);

    public Page<Label> findByNameContainingIgnoreCase(String name, Pageable pageable);

    public boolean existsByName(String name);

    public boolean existsByNameIgnoreCase(String name);

    public boolean existsByTicketsId(Long id);

    public List<Label> findByIsActiveTrue();

}