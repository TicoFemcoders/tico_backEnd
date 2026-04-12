package com.femcoders.tico.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.TicketsRepository;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Tickets;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.LabelMapper;

@Service
public class LabelServiceImpl implements LabelService {

  @Autowired
  private LabelRepository labelRepository;

  @Autowired
  private LabelMapper labelMapper;

  @Autowired
  private TicketsRepository ticketsRepository;

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

    Label updatedLabel = labelRepository.save(label);
    return labelMapper.toResponseDto(updatedLabel);
  }

  @Override
  @Transactional
  public void deleteLabel(Long id, boolean force) {
    Label label = labelRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));
    List<Tickets> associatedTickets = ticketsRepository.findByLabelsId(id);
    if (!associatedTickets.isEmpty()) {
      if (!force) {
        throw new IllegalStateException("La etiqueta está en uso por tickets activos. ¿Confirmas la eliminación?");
      }
      for (Tickets ticket : associatedTickets) {
        ticket.getLabels().remove(label);
      }
      ticketsRepository.saveAll(associatedTickets);
    }
    labelRepository.delete(label);
  }
}
