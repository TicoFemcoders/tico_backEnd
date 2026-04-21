package com.femcoders.tico.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.femcoders.tico.dto.request.AdminCreateUserReqDTO;
import com.femcoders.tico.dto.request.UpdateUserReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;

public interface UserService extends UserDetailsService {

    public UserResponseDTO createUser(AdminCreateUserReqDTO userDto);

    public List<UserResponseDTO> getAllUsers();

    public List<UserResponseDTO> getAllAdmins();

    public UserResponseDTO getUserById(Long id);

    public UserResponseDTO updateUser(Long id, UpdateUserReqDTO dto);

    public void deleteUser(Long userId, String reassignEmail);

    public UserResponseDTO toggleUserActive(Long id);
}
