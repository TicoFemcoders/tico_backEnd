package com.femcoders.tico.service;

import java.util.List;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;

public interface LabelService {

    public LabelResDTO createLabel(LabelReqDTO dto);

    public List<LabelResDTO> getAllLabels();

    public List<LabelResDTO> filterLabelsByName(String name);

    public LabelResDTO updateLabel(Long id, LabelReqDTO labelDTO);

    public void deactivateLabel(Long id);

    public LabelResDTO activateLabel(Long id);

}
