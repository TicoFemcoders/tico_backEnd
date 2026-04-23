package com.femcoders.tico.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.femcoders.tico.dto.request.AdminCreateUserRequest;
import com.femcoders.tico.dto.request.UpdateUserRequest;
import com.femcoders.tico.dto.response.UserResponse;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntity(UpdateUserRequest dto, @MappingTarget User entity);

    User toEntity(AdminCreateUserRequest dto);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "openTickets", constant = "0L")
    UserResponse toResponseDTO(User entity);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<UserRole> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> "ROLE_" + role.name())
                .collect(Collectors.toSet());
    }

}
