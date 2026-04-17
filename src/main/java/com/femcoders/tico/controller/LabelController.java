package com.femcoders.tico.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;
import com.femcoders.tico.service.LabelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/labels")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @PostMapping
    public ResponseEntity<LabelResDTO> createLabel(@Valid @RequestBody LabelReqDTO dto) {
        return new ResponseEntity<>(labelService.createLabel(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LabelResDTO>> getAllLabels() {
        return ResponseEntity.ok(labelService.getAllLabels());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<LabelResDTO>> searchLabels(@RequestParam String name) {
        return ResponseEntity.ok(labelService.filterLabelsByName(name));
    }

    @GetMapping("/{id}/active-tickets-count")
    public ResponseEntity<Map<String, Integer>> countActiveTickets(@PathVariable Long id) {
        int count = labelService.countActiveTicketsByLabel(id);
        return ResponseEntity.ok(Map.of("activeTickets", count));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabelResDTO> updateLabel(
            @PathVariable Long id,
            @RequestBody LabelReqDTO dto) {
        return ResponseEntity.ok(labelService.updateLabel(id, dto));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateLabel(@PathVariable Long id) {
        labelService.deactivateLabel(id);
        return ResponseEntity.noContent().build();
    }
}
