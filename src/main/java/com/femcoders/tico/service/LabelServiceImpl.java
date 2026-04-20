package com.femcoders.tico.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.femcoders.tico.dto.request.LabelReqDTO;
import com.femcoders.tico.dto.response.LabelResDTO;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.exception.ConflictException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.LabelMapper;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.TicketRepository;

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
            throw new ConflictException("La etiqueta '" + dto.name() + "' ya existe");
        }
        Label labelEntity = labelMapper.toEntity(dto);
        Label savedLabel = labelRepository.save(labelEntity);
        return labelMapper.toResponseDto(savedLabel);
    }

    // @Override
    // public List<LabelResDTO> getAllLabels() {
    // return labelRepository.findAll()
    // .stream()
    // .map(labelMapper::toResponseDto)
    // .toList();
    // }
    
    @Override
    public List<LabelResDTO> getAllLabels() {
        return labelRepository.findAll().stream()
                .map(label -> {
                    LabelResDTO dto = labelMapper.toResponseDto(label);

                    long activeCount = labelRepository.countActiveTicketsByLabelId(label.getId());
                    long closedCount = labelRepository.countClosedTicketsByLabelId(label.getId());

                    return new LabelResDTO(
                            dto.id(),
                            dto.name(),
                            dto.color(),
                            dto.createdAt(),
                            dto.active(),
                            activeCount,
                            closedCount);
                })
                .toList();
    }

    @Override
    public List<LabelResDTO> filterLabelsByName(String name) {
        return labelRepository.findByNameContainingIgnoreCase(name)
                .stream()
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

        label.setUpdatedAt(LocalDateTime.now());
        return labelMapper.toResponseDto(labelRepository.save(label));
    }

    @Override
    public int countActiveTicketsByLabel(Long id) {
        List<Ticket> tickets = ticketsRepository.findByLabelsId(id);
        return (int) tickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.CLOSED)
                .count();
    }

    @Override
    @Transactional
    public void deactivateLabel(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));

        int activeTickets = countActiveTicketsByLabel(id);

        if (activeTickets > 0) {
            throw new ConflictException(
                    "La etiqueta '" + label.getName() + "' está asignada a " + activeTickets
                    + " tickets activos y no puede desactivarse.");
        }

        label.setIsActive(false);
        labelRepository.save(label);
    }
}
