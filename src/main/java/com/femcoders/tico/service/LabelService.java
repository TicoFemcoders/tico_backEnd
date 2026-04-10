package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;

public interface LabelService {

    public LabelResDTO createLabel(LabelReqDTO dto);
    


}
