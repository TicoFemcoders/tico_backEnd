package com.femcoders.tico.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.femcoders.tico.dto.response.NotificationResponse;
import com.femcoders.tico.dto.response.NotificationSummaryResponse;
import com.femcoders.tico.service.NotificationService;

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

    @Test
    void markAsRead_ShouldReturnOk() {

        doNothing().when(notificationService).markAsRead(1L);

        ResponseEntity<Void> response = notificationController.markAsRead(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(notificationService, times(1)).markAsRead(1L);
    }

    @Test
    void markAllAsRead_ShouldReturnOk() {

        doNothing().when(notificationService).markAllAsRead();

        ResponseEntity<Void> response = notificationController.markAllAsRead();

        assertEquals(200, response.getStatusCode().value());
        verify(notificationService, times(1)).markAllAsRead();
    }
}
