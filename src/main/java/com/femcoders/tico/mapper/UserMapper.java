package com.femcoders.tico.mapper;

import com.femcoders.tico.dto.request.UserRegisterReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", source = "password") 
    @Mapping(target = "id", ignore = true)                
    @Mapping(target = "createdAt", ignore = true)         
    @Mapping(target = "updatedAt", ignore = true)         
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "stringsToRoles")     
    User toEntity(UserRegisterReqDTO dto);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserResponseDTO toResponseDTO(User entity);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<UserRole> roles) {
    if (roles == null) return Set.of();
    return roles.stream()
        .map(role -> "ROLE_" + role.name())
        .collect(Collectors.toSet());
  }

  @Named("stringsToRoles")
    default Set<UserRole> stringsToRoles(Set<String> rolesStrings) {
        if (rolesStrings == null|| rolesStrings.isEmpty()){
            return Set.of();
        } 
        return rolesStrings.stream()
            .map(roleStr -> {
               try {
                    return UserRole.valueOf(roleStr);
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Rol inválido: " + roleStr + ". Valores aceptados: ADMIN, EMPLOYEE");
                    
                    // BadRequestException("Rol inválido: " + roleStr +  ". Valores aceptados: ADMIN, EMPLOYEE");
                }
            })
            .collect(Collectors.toSet());
    }

}