package com.femcoders.tico.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

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

import com.femcoders.tico.dto.request.LabelRequest;
import com.femcoders.tico.dto.response.LabelResponse;
import com.femcoders.tico.service.LabelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabelResponse> createLabel(@Valid @RequestBody LabelRequest dto) {
        return new ResponseEntity<>(labelService.createLabel(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<LabelResponse>> getAllLabels(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(labelService.getAllLabels(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<LabelResponse>> searchLabels(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(labelService.filterLabelsByName(name, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabelResponse> updateLabel(
            @PathVariable Long id,
            @RequestBody LabelRequest dto) {
        return ResponseEntity.ok(labelService.updateLabel(id, dto));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateLabel(@PathVariable Long id) {
        labelService.deactivateLabel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabelResponse> activateLabel(@PathVariable Long id) {
        return ResponseEntity.ok(labelService.activateLabel(id));
    }
}
