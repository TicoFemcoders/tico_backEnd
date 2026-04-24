package com.femcoders.tico.controller;

import com.femcoders.tico.dto.response.NotificationResponseDTO;
import com.femcoders.tico.dto.response.NotificationSummaryDTO;
import com.femcoders.tico.service.NotificationService;
import com.femcoders.tico.dto.response.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;


    @Test
    void getPaginaNotifications_ShouldReturnSummary() {

        NotificationSummaryDTO summary = new NotificationSummaryDTO(3L, List.of());
        when(notificationService.getPaginatedSummary(0, 20)).thenReturn(summary);


        ResponseEntity<NotificationSummaryDTO> response =
            notificationController.getPaginatedNotifications(0, 20);


        assertEquals(200, response.getStatusCode().value());
        assertEquals(3L, response.getBody().getTotalUnread());
    }

    @Test
    void getUnread_ShouldReturnUnreadList() {
        NotificationResponseDTO dto = new NotificationResponseDTO(1L, 10L, "Mensaje", false, null);

        ResponseEntity<List<NotificationResponseDTO>> responde =
              notificationController.getUnread();
        
              
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());


    }

}
