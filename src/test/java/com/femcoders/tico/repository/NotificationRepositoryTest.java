package com.femcoders.tico.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femcoders.tico.entity.TicketMessage;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryTest {

    @Mock
    private TicketMessageRepository ticketMessageRepository;

    @Test
    void findByRecipientIdAndIsReadFalse_ShouldReturnUnreadNotifications() {

        TicketMessage notification = new TicketMessage();
        notification.setId(1L);
        notification.setRecipientId(1L);
        notification.setIsRead(false);
        notification.setContent("Mensaje de prueba");

        when(ticketMessageRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<TicketMessage> result
                = ticketMessageRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(1L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
    }

    @Test
    void countByRecipientIdAndIsReadFalse_ShouldReturnCount() {

        when(ticketMessageRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(3L);

        long count = ticketMessageRepository.countByRecipientIdAndIsReadFalse(1L);

        assertEquals(3L, count);
    }

    @Test
    void markAllAsReadByRecipient_ShouldReturnUpdatedCount() {

        when(ticketMessageRepository.markAllAsReadByRecipient(1L)).thenReturn(3);

        int updated = ticketMessageRepository.markAllAsReadByRecipient(1L);

        assertEquals(3, updated);
    }

    @Test
    void markAsReadByIdAndRecipient_ShouldReturnOne_WhenExists() {

        when(ticketMessageRepository.markAsReadByIdAndRecipient(1L, 1L)).thenReturn(1);

        int updated = ticketMessageRepository.markAsReadByIdAndRecipient(1L, 1L);

        assertEquals(1, updated);
    }
}
