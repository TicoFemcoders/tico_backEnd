package com.femcoders.tico.entity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;
import java.time.LocalDateTime;

class TicketTest {

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticket = new Ticket();
        ticket.setTitle("Test ticket");
        ticket.setDescription("Test description");
    }

    @Test
    void whenClose_thenStatusIsClosedAndClosedAtIsSet() {
        ticket.close();

        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        assertNotNull(ticket.getClosedAt());
        assertTrue(ticket.getClosedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void whenGenerateEmailSubject_thenFormatIsCorrect() {
        ticket.setId(1L);
        ticket.generateEmailSubject();

        assertEquals("[TICO-1] Test ticket", ticket.getEmailSubject());
    }

    @Test
    void whenNewTicket_thenDefaultStatusIsOpen() {
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
    }

    @Test
    void whenNewTicket_thenDefaultPriorityIsMedium() {
        assertEquals(TicketPriority.MEDIUM, ticket.getPriority());
    }
}