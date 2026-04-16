package com.femcoders.tico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;
import com.femcoders.tico.entity.Label;

@Mapper(componentModel = "spring")
public interface LabelMapper {

@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
@Mapping(target = "tickets", ignore = true)
@Mapping(target = "isActive" , ignore = true)
Label toEntity(LabelReqDTO dto);

LabelResDTO toResponseDto (Label entity);

}
