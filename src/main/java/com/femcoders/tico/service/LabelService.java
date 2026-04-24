package com.femcoders.tico.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.femcoders.tico.dto.request.LabelRequest;
import com.femcoders.tico.dto.response.LabelResponse;

public interface LabelService {

    public LabelResponse createLabel(LabelRequest dto);

    public Page<LabelResponse> getAllLabels(Pageable pageable);

    public Page<LabelResponse> filterLabelsByName(String name, Pageable pageable);

    public LabelResponse updateLabel(Long id, LabelRequest labelDTO);

    public void deactivateLabel(Long id);

    public LabelResponse activateLabel(Long id);

}
