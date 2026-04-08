package com.femcoders.tico.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.femcoders.tico.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

}
