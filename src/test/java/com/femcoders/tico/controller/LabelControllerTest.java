package com.femcoders.tico.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.femcoders.tico.dto.request.LabelRequest;
import com.femcoders.tico.dto.response.LabelResponse;
import com.femcoders.tico.service.LabelService;

@ExtendWith(MockitoExtension.class)
public class LabelControllerTest {

    @Mock
    private LabelService labelService;

    @InjectMocks
    private LabelController labelController;

    private LabelRequest mockRequest;
    private LabelResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockRequest = new LabelRequest("Software", "#ff0000");
        mockResponse = new LabelResponse(1L, "Software", "#ff0000", null, true, 0L, 0L);
    }

    @Test
    void createLabel_ShouldReturn201_WhenCreatedSuccessfully() {

        when(labelService.createLabel(mockRequest)).thenReturn(mockResponse);

        ResponseEntity<LabelResponse> response = labelController.createLabel(mockRequest);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("Software", response.getBody().name());
    }

    @Test
    void getActiveLabels_ShouldReturn200_WithList() {

        when(labelService.getActiveLabels()).thenReturn(List.of(mockResponse));

        ResponseEntity<List<LabelResponse>> response = labelController.getActiveLabels();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void deactivateLabel_ShouldReturn204_WhenDeactivated() {

        doNothing().when(labelService).deactivateLabel(1L);

        ResponseEntity<Void> response = labelController.deactivateLabel(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(labelService, times(1)).deactivateLabel(1L);
    }

    @Test
    void activateLabel_ShouldReturn200_WhenActivated() {

        when(labelService.activateLabel(1L)).thenReturn(mockResponse);

        ResponseEntity<LabelResponse> response = labelController.activateLabel(1L);


        assertEquals(200, response.getStatusCode().value());
        assertEquals("Software", response.getBody().name());
    }
}
