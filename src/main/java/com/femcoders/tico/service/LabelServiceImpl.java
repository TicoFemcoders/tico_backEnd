package com.femcoders.tico.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.LabelMapper;

@Service
public class LabelServiceImpl implements LabelService {

  @Autowired
  private LabelRepository labelRepository;

  @Autowired
  private LabelMapper labelMapper;

  @Autowired
  private TicketRepository ticketsRepository;

  @Override
  public LabelResDTO createLabel(LabelReqDTO dto) {

    if (labelRepository.existsByName(dto.name())) {
      throw new IllegalStateException("La etiqueta '" + dto.name() + "' ya existe");
    }
    Label labelEntity = labelMapper.toEntity(dto);

    Label savedLabel = labelRepository.save(labelEntity);

    return labelMapper.toResponseDto(savedLabel);
  }

  @Override
  public List<LabelResDTO> getAllLabels() {
    return labelRepository.findAll()
        .stream()
        .map(labelMapper::toResponseDto)
        .toList();
  }

  @Override
  public List<LabelResDTO> filterLabelsByName(String name) {
    return labelRepository.findByNameContainingIgnoreCase(name).stream()
        .map(labelMapper::toResponseDto)
        .toList();
  }

  @Override
  public LabelResDTO updateLabel(Long id, LabelReqDTO dto) {

    Label label = labelRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));

    label.setName(dto.name());
    label.setColor(dto.color());
    label.setUpdatedAt(LocalDateTime.now());

    Label updatedLabel = labelRepository.save(label);
    return labelMapper.toResponseDto(updatedLabel);
  }

  @Override
  @Transactional
  public void deactivateLabel(Long id) {
    Label label = labelRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));

    List<Ticket> associatedTickets = ticketsRepository.findByLabelsId(id);

    if (!associatedTickets.isEmpty()) {
      throw new IllegalStateException("La etiqueta está en uso por tickets activos.No se puede desactivar.");
    }

    label.setIsActive(false);
    labelRepository.save(label);
  }

}
