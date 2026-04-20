package com.femcoders.tico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.enums.TicketStatus;

@Mapper(componentModel = "spring")
public interface LabelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Label toEntity(LabelReqDTO dto);

    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "activeTickets", expression = "java(countActive(label))")
    @Mapping(target = "closedTickets", expression = "java(countClosed(label))")
    LabelResDTO toResponseDto(Label label);

    default long countActive(Label label) {
        return label.getTickets().stream()
                .filter(t -> t.getStatus() != TicketStatus.CLOSED)
                .count();
    }

    default long countClosed(Label label) {
        return label.getTickets().stream()
                .filter(t -> t.getStatus() == TicketStatus.CLOSED)
                .count();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntity(LabelReqDTO dto, @MappingTarget Label label);
}
