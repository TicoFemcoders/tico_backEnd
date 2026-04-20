package com.femcoders.tico.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.femcoders.tico.dto.request.TicketCreateReqDTO;
import com.femcoders.tico.dto.response.TicketResponseDTO;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Ticket;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "emailSubject", ignore = true)
    @Mapping(target = "labels", ignore = true)
    Ticket toEntity(TicketCreateReqDTO dto);

    @Mapping(target = "createdByName", source = "createdBy.name")
    @Mapping(target = "assignedToName", source = "assignedTo.name")
    @Mapping(target = "labels", source = "labels", qualifiedByName = "labelsToNames")
    @Mapping(target = "closingMessage", source = "closingMessage")
    TicketResponseDTO toResponseDTO(Ticket entity);

    @Named("labelsToNames")
    default Set<String> labelsToNames(Set<Label> labels) {
        if (labels == null) {
            return Set.of();
        }
        return labels.stream()
                .map(Label::getName)
                .collect(Collectors.toSet());
    }
}
