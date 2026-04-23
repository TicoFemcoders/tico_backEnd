package com.femcoders.tico.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femcoders.tico.dto.response.NotificationResponseDTO;
import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.repository.TicketMessageRepository;
import com.femcoders.tico.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private TicketMessageRepository ticketMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User mockUser;
    private TicketMessage mockNotification;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Ana Garcia");

        mockNotification = new TicketMessage();
        mockNotification.setId(1L);
        mockNotification.setTicketId(10L);
        mockNotification.setContent("Tu ticket ha sido actualizado");
        mockNotification.setIsRead(false);
        mockNotification.setRecipientId(1L);
        mockNotification.setAuthor(mockUser);
        mockNotification.setCreatedAt(LocalDateTime.now());

    }

    @Test
    void create_ShouldSaveNotificattion_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        notificationService.create(10L, 1L, 2L, "Nuevo mensaje");

        verify(ticketMessageRepository, times(1)).save(any(TicketMessage.class));

    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, ()
                -> notificationService.create(10L, 99L, 2L, "Mensaje")
        );

    }

    @Test
    void getUnread_ShouldReturnUnreadNotifications() {
        
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(ticketMessageRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(mockNotification));

        
        List<NotificationResponseDTO> result = notificationService.getUnread();

        
        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void markAsRead_ShouldMarkNotificationAsRead() {
        
        when(ticketMessageRepository.findById(1L)).thenReturn(Optional.of(mockNotification));

        
        notificationService.markAsRead(1L);

        
        assertTrue(mockNotification.getIsRead());
        verify(ticketMessageRepository, times(1)).save(mockNotification);
    }

    @Test
    void markAllAsRead_ShouldMarkAllNotificationsAsRead() {
        
        when(authService.getAuthenticatedUser()).thenReturn(mockUser);
        when(ticketMessageRepository.findByRecipientIdAndIsReadFalse(1L))
                .thenReturn(List.of(mockNotification));

        
        notificationService.markAllAsRead();

        
        assertTrue(mockNotification.getIsRead());
        verify(ticketMessageRepository, times(1)).saveAll(any());
    }

}
