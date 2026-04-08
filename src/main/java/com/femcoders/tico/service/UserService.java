package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.UserRegisterReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;

public interface UserService {

    public UserResponseDTO createUser(UserRegisterReqDTO userDto);
}
