package com.femcoders.tico.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.femcoders.tico.dto.request.LabelRequestDTO;
import com.femcoders.tico.dto.response.LabelResponseDTO;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.LabelMapper;
import com.femcoders.tico.repository.LabelRepository;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

  private final LabelRepository labelRepository;
  private final LabelMapper labelMapper;

  @Override
  public LabelResponseDTO createLabel(LabelRequestDTO dto) {

    if (labelRepository.existsByName(dto.name())) {
      throw new IllegalStateException("La etiqueta '" + dto.name() + "' ya existe");
    }
    Label labelEntity = labelMapper.toEntity(dto);

    Label savedLabel = labelRepository.save(labelEntity);

    return labelMapper.toResponseDto(savedLabel);
  }

  @Override
  public List<LabelResponseDTO> getAllLabels() {
    return labelRepository.findAll().stream()
        .map(labelMapper::toResponseDto)
        .toList();
  }

  @Override
  public List<LabelResponseDTO> filterLabelsByName(String name) {
    return labelRepository.findByNameContainingIgnoreCase(name).stream()
        .map(labelMapper::toResponseDto)
        .toList();
  }

  @Override
  public LabelResponseDTO updateLabel(Long id, LabelRequestDTO dto) {
    Label label = labelRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));
    labelMapper.updateEntity(dto, label);
    return labelMapper.toResponseDto(labelRepository.save(label));
  }

  @Override
  @Transactional
  public void deactivateLabel(Long id) {
    Label label = labelRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));

    boolean hasActiveTickets = label.getTickets().stream()
        .anyMatch(t -> t.getStatus() != TicketStatus.CLOSED);

    if (hasActiveTickets) {
      throw new IllegalStateException("La etiqueta está en uso por tickets activos. No se puede desactivar.");
    }

    label.setIsActive(false);
    labelRepository.save(label);
  }

  @Override
  public LabelResponseDTO activateLabel(Long id) {
    Label label = labelRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));
    label.setIsActive(true);
    label.setUpdatedAt(LocalDateTime.now());
    return labelMapper.toResponseDto(labelRepository.save(label));
  }

}