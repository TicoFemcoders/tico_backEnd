package com.femcoders.tico.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketStatus;

@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    private User admin;
    private User employee;
    private Label label;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setName("Employee Test");
        employee.setEmail("employee@test.com");
        employee.setPasswordHash("hash");
        userRepository.save(employee);

        admin = new User();
        admin.setName("Admin Test");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("hash");
        userRepository.save(admin);

        label = new Label();
        label.setName("Bug");
        label.setColor("#FF0000");
        labelRepository.save(label);
    }

    private Ticket createTicket(String title, TicketStatus status, User assignedTo) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription("Description for " + title);
        ticket.setStatus(status);
        ticket.setCreatedBy(employee);
        ticket.setAssignedTo(assignedTo);
        return ticketRepository.save(ticket);
    }

    @Test
    void whenCountOpenTicketsPerUser_thenReturnsCorrectCount() {
        createTicket("Ticket 1", TicketStatus.OPEN, admin);
        createTicket("Ticket 2", TicketStatus.OPEN, admin);
        createTicket("Ticket 3", TicketStatus.CLOSED, admin);

        List<Object[]> result = ticketRepository.countOpenTicketsPerUser();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0)[1]);
    }

    @Test
    void whenCountTicketsGroupedByLabelAndStatus_thenReturnsCorrectData() {
        Ticket ticket = createTicket("Ticket con label", TicketStatus.OPEN, admin);
        ticket.getLabels().add(label);
        ticketRepository.save(ticket);

        List<Object[]> result = ticketRepository.countTicketsGroupedByLabelAndStatus();

        assertEquals(1, result.size());
        assertEquals(label.getId(), result.get(0)[0]);
        assertEquals(TicketStatus.OPEN, result.get(0)[1]);
        assertEquals(1L, result.get(0)[2]);
    }

    @Test
    void whenUnassignOpenTicketsByAdmin_thenOpenTicketsAreUnassigned() {
        createTicket("Ticket abierto", TicketStatus.OPEN, admin);
        createTicket("Ticket cerrado", TicketStatus.CLOSED, admin);

        int updated = ticketRepository.unassignOpenTicketsByAdmin(admin.getId());

        assertEquals(1, updated);
        List<Ticket> assignedOpen = ticketRepository.findByAssignedToIdAndStatusNot(
            admin.getId(), TicketStatus.CLOSED);
        assertTrue(assignedOpen.isEmpty());
    }

    @Test
    void whenFindByAssignedToIdAndStatusNot_thenExcludesClosedTickets() {
        createTicket("Ticket abierto", TicketStatus.OPEN, admin);
        createTicket("Ticket cerrado", TicketStatus.CLOSED, admin);

        List<Ticket> result = ticketRepository.findByAssignedToIdAndStatusNot(
            admin.getId(), TicketStatus.CLOSED);

        assertEquals(1, result.size());
        assertEquals(TicketStatus.OPEN, result.get(0).getStatus());
    }
}