package com.femcoders.tico.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.femcoders.tico.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

     public Optional<User> findByEmail(String email);
}
