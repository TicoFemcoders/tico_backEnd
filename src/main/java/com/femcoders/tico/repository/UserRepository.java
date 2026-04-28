package com.femcoders.tico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findByEmail(String email);

    public List<User> findByRolesContaining(UserRole role);

    public Page<User> findByRolesContaining(UserRole role, Pageable pageable);

    public Page<User> findByRolesContainingAndIsActiveTrue(UserRole role, Pageable pageable);

    public long countByRolesContaining(UserRole role);
}
