package com.femcoders.tico.service;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femcoders.tico.dto.request.LabelRequest;
import com.femcoders.tico.dto.response.LabelResponse;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.LabelMapper;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class LabelServiceImplTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private LabelMapper labelMapper;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private LabelServiceImpl labelService;

    private Label mockLabel;
    private LabelRequest mockRequest;
    private LabelResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockLabel = new Label();
        mockLabel.setId(1L);
        mockLabel.setName("Software");
        mockLabel.setColor("#ff0000");
        mockLabel.setIsActive(true);

        mockRequest = new LabelRequest("Software", "#ff0000");

        mockResponse = new LabelResponse(1L, "Software", "#ff0000", null, true, 0L, 0L);
    }
    private Ticket createActiveTicket() {
    Ticket ticket = new Ticket();
    ticket.setStatus(TicketStatus.OPEN);
    return ticket;
}


    @Test
    void createLabel_ShouldReturnLabel_WhenCreatedSuccessfully() {

        when(labelRepository.existsByNameIgnoreCase("Software")).thenReturn(false);
        when(labelMapper.toEntity(mockRequest)).thenReturn(mockLabel);
        when(labelRepository.save(mockLabel)).thenReturn(mockLabel);
        when(labelMapper.toResponseDto(mockLabel)).thenReturn(mockResponse);

        LabelResponse result = labelService.createLabel(mockRequest);

        assertNotNull(result);
        assertEquals("Software", result.name());
    }

    @Test
    void createLabel_ShouldThrowException_WhenLabelAlreadyExists() {

        when(labelRepository.existsByNameIgnoreCase("Software")).thenReturn(true);

        assertThrows(IllegalStateException.class, ()
                -> labelService.createLabel(mockRequest)
        );
    }

    @Test
    void updateLabel_ShouldThrowException_WhenLabelNotFound() {

        when(labelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, ()
                -> labelService.updateLabel(99L, mockRequest)
        );
    }

    @Test
    void deactivateLabel_ShouldThrowException_WhenHasActiveTickets() {

        when(labelRepository.findById(1L)).thenReturn(Optional.of(mockLabel));
        mockLabel.setTickets(Set.of(createActiveTicket()));
        assertThrows(BadRequestException.class, ()
                -> labelService.deactivateLabel(1L)
        );
    }
}
