package com.femcoders.tico.service;

import java.util.List;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;

public interface LabelService {

    public LabelResDTO createLabel(LabelReqDTO dto);

    List<LabelResDTO> getAllLabels();

    List<LabelResDTO> filterLabelsByName(String name);

    LabelResDTO updateLabel(Long id, LabelReqDTO labelDTO);

    int countActiveTicketsByLabel(Long id);

    void deactivateLabel(Long id);

}
