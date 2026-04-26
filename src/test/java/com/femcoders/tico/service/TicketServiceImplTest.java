package com.femcoders.tico.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.femcoders.tico.dto.request.TicketCreateRequest;
import com.femcoders.tico.dto.response.TicketResponse;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.TicketMapper;
import com.femcoders.tico.repository.LabelRepository;
import com.femcoders.tico.repository.TicketRepository;
import com.femcoders.tico.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private UserRepository userRepository;
    @Mock private LabelRepository labelRepository;
    @Mock private TicketMapper ticketMapper;
    @Mock private AuthService authService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User employee;
    private User admin;
    private Ticket ticket;
    private TicketResponse ticketResponse;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setId(1L);
        employee.setName("Employee Test");
        employee.setEmail("employee@test.com");
        employee.setRoles(Set.of(UserRole.EMPLOYEE));

        admin = new User();
        admin.setId(2L);
        admin.setName("Admin Test");
        admin.setEmail("admin@test.com");
        admin.setIsActive(true);
        admin.setRoles(Set.of(UserRole.ADMIN));

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("Titulo test");
        ticket.setDescription("Descripcion test larga");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setCreatedBy(employee);
        ticket.setLabels(new HashSet<>());

        ticketResponse = new TicketResponse(
                1L, "Titulo test", "Descripcion test larga",
                TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Employee Test", null, null,
                Set.of(), null, null, null, null, null);
    }

    // ---- createTicket ----

    @Test
    void whenCreateTicket_thenReturnsTicketResponse() {
        TicketCreateRequest request = new TicketCreateRequest(
                "Titulo test", "Descripcion test larga", TicketPriority.MEDIUM, List.of());

        when(authService.getAuthenticatedUser()).thenReturn(employee);
        when(ticketMapper.toEntity(request)).thenReturn(ticket);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponseDTO(ticket)).thenReturn(ticketResponse);

        TicketResponse result = ticketService.createTicket(request);

        assertNotNull(result);
        assertEquals("Titulo test", result.title());
        verify(ticketRepository, times(1)).save(ticket);
    }

    // ---- assignAdmin ----

    @Test
    void whenAssignAdmin_thenAdminIsAssigned() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(authService.getAuthenticatedUser()).thenReturn(admin);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponseDTO(ticket)).thenReturn(ticketResponse);

        TicketResponse result = ticketService.assignAdmin(1L, 2L);

        assertNotNull(result);
        assertEquals(admin, ticket.getAssignedTo());
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void whenAssignAdmin_withInactiveAdmin_thenThrowsBadRequestException() {
        admin.setIsActive(false);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

        assertThrows(BadRequestException.class,
                () -> ticketService.assignAdmin(1L, 2L));
    }

    @Test
    void whenAssignAdmin_withNonExistentTicket_thenThrowsResourceNotFoundException() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ticketService.assignAdmin(99L, 2L));
    }

    @Test
    void whenAssignAdmin_withNonExistentAdmin_thenThrowsResourceNotFoundException() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ticketService.assignAdmin(1L, 99L));
    }

    // ---- closeTicket ----

    @Test
    void whenCloseTicket_thenStatusIsClosed() {
        ticket.setAssignedTo(admin);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(authService.getAuthenticatedUser()).thenReturn(admin);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponseDTO(ticket)).thenReturn(ticketResponse);

        ticketService.closeTicket(1L, "Mensaje de cierre");

        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        assertNotNull(ticket.getClosedAt());
    }

    @Test
    void whenCloseTicket_alreadyClosed_thenReturnsWithoutSaving() {
        ticket.setAssignedTo(admin);
        ticket.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(authService.getAuthenticatedUser()).thenReturn(admin);
        when(ticketMapper.toResponseDTO(ticket)).thenReturn(ticketResponse);

        ticketService.closeTicket(1L, null);

        verify(ticketRepository, never()).save(any());
    }

    // ---- reopenTicket ----

    @Test
    void whenReopenTicket_thenStatusIsOpen() {
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setAssignedTo(admin);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(authService.getAuthenticatedUser()).thenReturn(admin);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponseDTO(ticket)).thenReturn(ticketResponse);

        ticketService.reopenTicket(1L);

        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        assertNull(ticket.getClosedAt());
    }

    @Test
    void whenReopenTicket_notClosed_thenThrowsBadRequestException() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class,
                () -> ticketService.reopenTicket(1L));
    }

    @Test
    void whenReopenTicket_withNonExistentTicket_thenThrowsResourceNotFoundException() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ticketService.reopenTicket(99L));
    }

    // ---- getTicketById ----

    @Test
    void whenGetTicketById_asEmployee_thenReturnsOwnTicket() {
        when(authService.getAuthenticatedUser()).thenReturn(employee);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponseDTO(ticket)).thenReturn(ticketResponse);

        TicketResponse result = ticketService.getTicketById(1L);

        assertNotNull(result);
    }

    @Test
    void whenGetTicketById_asEmployee_withOtherUsersTicket_thenThrowsResourceNotFoundException() {
        User otherEmployee = new User();
        otherEmployee.setId(99L);
        otherEmployee.setRoles(Set.of(UserRole.EMPLOYEE));
        ticket.setCreatedBy(otherEmployee);

        when(authService.getAuthenticatedUser()).thenReturn(employee);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ResourceNotFoundException.class,
                () -> ticketService.getTicketById(1L));
    }
}