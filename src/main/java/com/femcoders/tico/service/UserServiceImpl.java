package com.femcoders.tico.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.femcoders.tico.dto.request.UserRegisterReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.mapper.UserMapper;
import com.femcoders.tico.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserResponseDTO createUser(UserRegisterReqDTO userDto) {
       
        User user = userMapper.toEntity(userDto);
        
        User savedUser = userRepository.save(user);
        
        return userMapper.toResponseDTO(savedUser);
    }
}
