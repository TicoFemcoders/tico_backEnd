package com.femcoders.tico.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.femcoders.tico.dto.request.LabelRequestDTO;
import com.femcoders.tico.dto.response.LabelResponseDTO;

public interface LabelService {

    public LabelResponseDTO createLabel(LabelRequestDTO dto);

    public Page<LabelResponseDTO> getAllLabels(Pageable pageable);

    public Page<LabelResponseDTO> filterLabelsByName(String name, Pageable pageable);

    public LabelResponseDTO updateLabel(Long id, LabelRequestDTO labelDTO);

    public void deactivateLabel(Long id);

    public LabelResponseDTO activateLabel(Long id);

}
