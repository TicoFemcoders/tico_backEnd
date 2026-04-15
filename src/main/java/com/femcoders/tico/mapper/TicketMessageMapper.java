package com.femcoders.tico.mapper;

import org.mapstruct.Mapper;
import com.femcoders.tico.dto.TicketMessageRequestDTO;
import com.femcoders.tico.dto.TicketMessageResponseDTO;
import com.femcoders.tico.entity.TicketMessage;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticketId", ignore = true)
    @Mapping(target = "isRead", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TicketMessage toEntity(TicketMessageRequestDTO dto);

    TicketMessageResponseDTO toResponseDTO(TicketMessage entity);

}
