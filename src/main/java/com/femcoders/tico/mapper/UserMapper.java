package com.femcoders.tico.mapper;

import com.femcoders.tico.dto.request.UserRegisterReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;
import com.femcoders.tico.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", source = "password") 
    @Mapping(target = "id", ignore = true)                
    @Mapping(target = "createdAt", ignore = true)         
    @Mapping(target = "updatedAt", ignore = true)         
    @Mapping(target = "isActive", constant = "true")     
    User toEntity(UserRegisterReqDTO dto);

    UserResponseDTO toResponseDTO(User entity);
}