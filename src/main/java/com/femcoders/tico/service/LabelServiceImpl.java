package com.femcoders.tico.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.femcoders.tico.dto.LabelTicketCounts;
import com.femcoders.tico.dto.request.LabelRequest;
import com.femcoders.tico.dto.response.LabelResponse;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ConflictException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.LabelMapper;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.TicketRepository;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

  private final LabelRepository labelRepository;
  private final LabelMapper labelMapper;
  private final TicketRepository ticketRepository;

  @Override
  public LabelResponse createLabel(LabelRequest dto) {

    if (labelRepository.existsByNameIgnoreCase(dto.name())) {
      throw new IllegalStateException("La etiqueta '" + dto.name() + "' ya existe");
    }
    Label labelEntity = labelMapper.toEntity(dto);

    Label savedLabel = labelRepository.save(labelEntity);

    return labelMapper.toResponseDto(savedLabel);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<LabelResponse> getAllLabels(Pageable pageable) {
    LabelTicketCounts counts = LabelTicketCounts.from(
        ticketRepository.countTicketsGroupedByLabelAndStatus());
    return labelRepository.findAll(pageable)
        .map(label -> new LabelResponse(
            label.getId(),
            label.getName(),
            label.getColor(),
            label.getCreatedAt(),
            label.getIsActive(),
            counts.activeFor(label.getId()),
            counts.closedFor(label.getId())));
  }

  @Override
  public Page<LabelResponse> filterLabelsByName(String name, Pageable pageable) {
    return labelRepository.findByNameContainingIgnoreCase(name, pageable)
        .map(labelMapper::toResponseDto);
  }

  @Override
  public LabelResponse updateLabel(Long id, LabelRequest dto) {
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
      throw new BadRequestException("La etiqueta está en uso por tickets activos. No se puede desactivar.");
    }

    label.setIsActive(false);
    labelRepository.save(label);
  }

  @Override
  public LabelResponse activateLabel(Long id) {
    Label label = labelRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));
    label.setIsActive(true);
    label.setUpdatedAt(LocalDateTime.now());
    return labelMapper.toResponseDto(labelRepository.save(label));
  }

}