package com.femcoders.tico.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.mapper.LabelMapper;

@Service
public class LabelServiceImpl implements LabelService{

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper labelMapper;

    @Override
    public LabelResDTO createLabel(LabelReqDTO dto) {
        
    if (labelRepository.existsByName(dto.name())) {
            throw new RuntimeException("La etiqueta '" + dto.name() + "' ya existe");
        }
      Label labelEntity = labelMapper.toEntity(dto);

      Label savedLabel = labelRepository.save(labelEntity);

      return labelMapper.toResponseDto(savedLabel);
    }

}
