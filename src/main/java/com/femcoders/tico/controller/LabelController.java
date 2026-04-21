package com.femcoders.tico.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.request.LabelRequestDTO;
import com.femcoders.tico.dto.response.LabelResponseDTO;
import com.femcoders.tico.service.LabelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<LabelResponseDTO> createLabel(@Valid @RequestBody LabelRequestDTO dto) {
        return new ResponseEntity<>(labelService.createLabel(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LabelResponseDTO>> getAllLabels() {
        return ResponseEntity.ok(labelService.getAllLabels());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<LabelResponseDTO>> searchLabels(@RequestParam String name) {
        return ResponseEntity.ok(labelService.filterLabelsByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabelResponseDTO> updateLabel(
            @PathVariable Long id,
            @RequestBody LabelRequestDTO dto) {
        return ResponseEntity.ok(labelService.updateLabel(id, dto));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateLabel(@PathVariable Long id) {
        labelService.deactivateLabel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<LabelResponseDTO> activateLabel(@PathVariable Long id) {
        return ResponseEntity.ok(labelService.activateLabel(id));
    }
}
