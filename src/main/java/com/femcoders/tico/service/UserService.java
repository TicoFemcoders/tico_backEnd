package com.femcoders.tico.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;
 
import com.femcoders.tico.dto.request.AdminCreateUserReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;

public interface UserService extends UserDetailsService{

     UserResponseDTO createUser(AdminCreateUserReqDTO userDto);
 
    List<UserResponseDTO> getAllUsers();
 
    void deleteUser(Long userId, String reassignEmail);
}
