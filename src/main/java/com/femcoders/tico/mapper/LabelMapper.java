package com.femcoders.tico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.femcoders.tico.dto.request.LabelRequestDTO;
import com.femcoders.tico.dto.response.LabelResponseDTO;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.utils.LabelMappingUtils;

@Mapper(componentModel = "spring", uses = LabelMappingUtils.class)
public interface LabelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Label toEntity(LabelRequestDTO dto);

    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "activeTickets", source = "label", qualifiedByName = "countActiveTickets")
    @Mapping(target = "closedTickets", source = "label", qualifiedByName = "countClosedTickets")
    LabelResponseDTO toResponseDto(Label label);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntity(LabelRequestDTO dto, @MappingTarget Label label);
}
