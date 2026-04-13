package com.femcoders.tico.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;
import com.femcoders.tico.service.LabelService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping
    public ResponseEntity<List<LabelResDTO>> getAllLabels() {
        return ResponseEntity.ok(labelService.getAllLabels());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<LabelResDTO>> searchLabels(@RequestParam String name) {
        return ResponseEntity.ok(labelService.filterLabelsByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabelResDTO> updateLabel(
            @PathVariable Long id,
            @RequestBody LabelReqDTO dto) {

        LabelResDTO updatedLabel = labelService.updateLabel(id, dto);

        return ResponseEntity.ok(updatedLabel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(
            @PathVariable Long id,
            @RequestParam(name = "force", defaultValue = "false") boolean force) {

        labelService.deleteLabel(id, force);

        return ResponseEntity.noContent().build();
    }
}
