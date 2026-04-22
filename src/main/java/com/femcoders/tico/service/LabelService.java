package com.femcoders.tico.service;

import java.util.List;

import com.femcoders.tico.dto.request.LabelRequestDTO;
import com.femcoders.tico.dto.response.LabelResponseDTO;

public interface LabelService {

    public LabelResponseDTO createLabel(LabelRequestDTO dto);

    public List<LabelResponseDTO> getAllLabels();

    public List<LabelResponseDTO> filterLabelsByName(String name);

    public LabelResponseDTO updateLabel(Long id, LabelRequestDTO labelDTO);

    public void deactivateLabel(Long id);

    public LabelResponseDTO activateLabel(Long id);

}
