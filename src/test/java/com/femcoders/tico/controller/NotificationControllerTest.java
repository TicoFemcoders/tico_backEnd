package com.femcoders.tico.controller;

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

        NotificationSummaryResponse summary = new NotificationSummaryResponse(3L, List.of());
        when(notificationService.getPaginatedSummary(0, 20)).thenReturn(summary);

        ResponseEntity<NotificationSummaryResponse> response = notificationController.getPaginatedNotifications(0, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(3L, response.getBody().totalUnread());
    }

    @Test
    void getUnread_ShouldReturnUnreadList() {
        NotificationResponse dto = new NotificationResponse(1L, 10L, "Mensaje", false, null);
        when(notificationService.getUnread()).thenReturn(List.of(dto));

        ResponseEntity<List<NotificationResponse>> response = notificationController.getUnread();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

}
