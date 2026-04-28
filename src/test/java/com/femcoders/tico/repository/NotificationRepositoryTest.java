package com.femcoders.tico.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.entity.User;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TicketMessageRepository ticketMessageRepository;

    @Autowired
    private UserRepository userRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setName("Ana Garcia");
        mockUser.setEmail("ana@cohispania.com");
        mockUser.setPasswordHash("password");
        userRepository.save(mockUser);
    }

    private TicketMessage createNotification(boolean isRead) {
        TicketMessage notification = new TicketMessage();
        notification.setTicketId(10L);
        notification.setContent("Mensaje de prueba");
        notification.setIsRead(isRead);
        notification.setRecipientId(mockUser.getId());
        notification.setAuthor(mockUser);
        return ticketMessageRepository.save(notification);
    }

    @Test
    void findByRecipientIdAndIsReadFalse_ShouldReturnUnreadNotifications() {
        createNotification(false);
        createNotification(true);

        List<TicketMessage> result =
            ticketMessageRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(mockUser.getId());

        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
    }

    @Test
    void countByRecipientIdAndIsReadFalse_ShouldReturnCount() {
        createNotification(false);
        createNotification(false);
        createNotification(true);

        long count = ticketMessageRepository.countByRecipientIdAndIsReadFalse(mockUser.getId());

        assertEquals(2L, count);
    }

    @Test
    void markAllAsReadByRecipient_ShouldMarkAllAsRead() {
        createNotification(false);
        createNotification(false);

        int updated = ticketMessageRepository.markAllAsReadByRecipient(mockUser.getId());

        assertEquals(2, updated);
    }

    @Test
    void markAsReadByIdAndRecipient_ShouldMarkOneAsRead() {
        TicketMessage notification = createNotification(false);

        int updated = ticketMessageRepository.markAsReadByIdAndRecipient(
            notification.getId(), mockUser.getId());

        assertEquals(1, updated);
    }

    @Test
    void markAsReadByIdAndRecipient_ShouldReturnZero_WhenNotExists() {
        int updated = ticketMessageRepository.markAsReadByIdAndRecipient(99L, mockUser.getId());

        assertEquals(0, updated);
    }
}
