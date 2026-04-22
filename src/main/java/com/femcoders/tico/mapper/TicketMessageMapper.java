package com.femcoders.tico.mapper;

import org.mapstruct.Mapper;

import com.femcoders.tico.dto.request.TicketMessageRequest;
import com.femcoders.tico.dto.response.TicketMessageResponse;
import com.femcoders.tico.entity.TicketMessage;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticketId", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "isRead", ignore = true)
    @Mapping(target = "createdAt", ignore = true)

    TicketMessage toEntity(TicketMessageRequest dto);

    @Mapping(target = "authorName", source = "author.name")
    TicketMessageResponse toResponseDTO(TicketMessage entity);

}
