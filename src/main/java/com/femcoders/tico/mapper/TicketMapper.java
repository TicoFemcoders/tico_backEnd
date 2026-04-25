package com.femcoders.tico.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.femcoders.tico.dto.request.TicketCreateRequest;
import com.femcoders.tico.dto.response.LabelSummary;
import com.femcoders.tico.dto.response.TicketResponse;
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
    Ticket toEntity(TicketCreateRequest dto);

    @Mapping(target = "createdByName", source = "createdBy.name")
    @Mapping(target = "assignedToId", source = "assignedTo.id")
    @Mapping(target = "assignedToName", source = "assignedTo.name")
    @Mapping(target = "labels", source = "labels", qualifiedByName = "labelsToSummaries")
    @Mapping(target = "closingMessage", source = "closingMessage")
    TicketResponse toResponseDTO(Ticket entity);

    @Named("labelsToSummaries")
    default Set<LabelSummary> labelsToSummaries(Set<Label> labels) {
        if (labels == null) {
            return Set.of();
        }
        return labels.stream()
                .map(l -> new LabelSummary(l.getName(), l.getColor(), l.getIsActive()))
                .collect(Collectors.toSet());
    }
}
