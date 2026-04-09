package com.femcoders.tico.mapper;

import com.femcoders.tico.dto.request.UserRegisterReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", source = "password") 
    @Mapping(target = "id", ignore = true)                
    @Mapping(target = "createdAt", ignore = true)         
    @Mapping(target = "updatedAt", ignore = true)         
    @Mapping(target = "isActive", constant = "true")     
    User toEntity(UserRegisterReqDTO dto);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserResponseDTO toResponseDTO(User entity);

    @Named("rolesToStrings")
    default List<String> rolesToStrings(Set<UserRole> roles) {
    if (roles == null) return List.of();
    return roles.stream()
        .map(role -> "ROLE_" + role.name())
        .collect(Collectors.toList());
  }
}