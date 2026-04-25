package com.femcoders.tico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRolesContaining(UserRole role);

    Page<User> findByRolesContaining(UserRole role, Pageable pageable);

    Page<User> findByRolesContainingAndIsActiveTrue(UserRole role, Pageable pageable);

    long countByRolesContaining(UserRole role);
}
