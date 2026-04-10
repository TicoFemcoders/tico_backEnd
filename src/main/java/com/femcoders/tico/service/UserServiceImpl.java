package com.femcoders.tico.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.femcoders.tico.dto.request.UserRegisterReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.mapper.UserMapper;
import com.femcoders.tico.repository.UserRepository;
import com.femcoders.tico.security.UserDetail;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRegisterReqDTO userDto) {
       
        User user = userMapper.toEntity(userDto);

        user.setPasswordHash(passwordEncoder.encode(userDto.password()));
        
        User savedUser = userRepository.save(user);
        
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
        .map(UserDetail::new)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
