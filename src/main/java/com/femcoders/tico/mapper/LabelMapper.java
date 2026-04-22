package com.femcoders.tico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.femcoders.tico.dto.request.LabelRequest;
import com.femcoders.tico.dto.response.LabelResponse;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.utils.LabelMappingUtils;

@Mapper(componentModel = "spring", uses = LabelMappingUtils.class)
public interface LabelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Label toEntity(LabelRequest dto);

    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "activeTickets", source = "label", qualifiedByName = "countActiveTickets")
    @Mapping(target = "closedTickets", source = "label", qualifiedByName = "countClosedTickets")
    LabelResponse toResponseDto(Label label);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntity(LabelRequest dto, @MappingTarget Label label);
}
