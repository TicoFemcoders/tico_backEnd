package com.femcoders.tico.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.femcoders.tico.dto.request.AdminCreateUserRequest;
import com.femcoders.tico.dto.request.UpdateUserRequest;
import com.femcoders.tico.dto.response.UserResponse;

public interface UserService extends UserDetailsService {

    public UserResponse createUser(AdminCreateUserRequest userDto);

    public Page<UserResponse> getAllUsers(Pageable pageable);

    public Page<UserResponse> getAllAdmins(Pageable pageable);

    public UserResponse getUserById(Long id);

    public UserResponse updateUser(Long id, UpdateUserRequest dto);

    public void deleteUser(Long userId, String reassignEmail);

    public UserResponse toggleUserActive(Long id, String reassignEmail);
}
