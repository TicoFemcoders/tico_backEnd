package com.femcoders.tico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

     public Optional<User> findByEmail(String email);

     public List<User> findByRolesContaining(UserRole role);
}
