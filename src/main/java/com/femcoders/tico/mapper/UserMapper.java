package com.femcoders.tico.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.femcoders.tico.dto.request.AdminCreateUserReqDTO;
import com.femcoders.tico.dto.request.UpdateUserReqDTO;
import com.femcoders.tico.dto.response.UserResponseDTO;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "false")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "stringsToRoles")
    void updateEntity(UpdateUserReqDTO dto, @MappingTarget User entity);

    User toEntity(AdminCreateUserReqDTO dto);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "openTickets", constant = "0")
    UserResponseDTO toResponseDTO(User entity);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<UserRole> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> "ROLE_" + role.name())
                .collect(Collectors.toSet());
    }

    @Named("stringsToRoles")
    default Set<UserRole> stringsToRoles(Set<String> rolesStrings) {
        if (rolesStrings == null || rolesStrings.isEmpty()) {
            return Set.of();
        }
        return rolesStrings.stream()
                .map(roleStr -> {
                    try {
                        return UserRole.valueOf(roleStr);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Rol inválido: " + roleStr + ". Valores aceptados: ADMIN, EMPLOYEE");
                    }
                })
                .collect(Collectors.toSet());
    }

}
