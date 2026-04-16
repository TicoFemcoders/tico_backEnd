package com.femcoders.tico.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.femcoders.tico.dto.request.AdminCreateUserReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;

public interface UserService extends UserDetailsService{

    public UserResponseDTO createUser(AdminCreateUserReqDTO userDto);
}
