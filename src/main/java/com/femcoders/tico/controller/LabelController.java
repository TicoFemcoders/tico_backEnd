package com.femcoders.tico.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;
import com.femcoders.tico.service.LabelService;


import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("api/labels")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @PostMapping
    public ResponseEntity<LabelResDTO> createLabel(@Valid @RequestBody LabelReqDTO dto) {
       LabelResDTO response = labelService.createLabel(dto);
       return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
}
