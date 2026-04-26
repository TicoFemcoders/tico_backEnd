package com.femcoders.tico.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.femcoders.tico.dto.request.AssignAdminRequest;
import com.femcoders.tico.dto.request.ChangePriorityRequest;
import com.femcoders.tico.dto.request.ChangeStatusRequest;
import com.femcoders.tico.dto.request.CloseTicketRequest;
import com.femcoders.tico.dto.request.TicketCreateRequest;
import com.femcoders.tico.dto.response.TicketResponse;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.service.TicketService;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private TicketResponse ticketResponse;

    @BeforeEach
    void setUp() {
        ticketResponse = new TicketResponse(
                1L, "Titulo test", "Descripcion test larga",
                TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Employee Test", null, null,
                Set.of(), null, null, null, null, null);
    }


    @Test
    void whenCreateTicket_thenReturns201() {
        TicketCreateRequest request = new TicketCreateRequest(
                "Titulo test", "Descripcion test larga", TicketPriority.MEDIUM, List.of());
        when(ticketService.createTicket(request)).thenReturn(ticketResponse);

        ResponseEntity<TicketResponse> result = ticketController.createTicket(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("Titulo test", result.getBody().title());
        verify(ticketService, times(1)).createTicket(request);
    }


    @Test
    void whenGetAllTickets_thenReturns200() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<TicketResponse> page = new PageImpl<>(List.of(ticketResponse));
        when(ticketService.getAllTickets(pageable)).thenReturn(page);

        ResponseEntity<Page<TicketResponse>> result = ticketController.getAllTickets(pageable);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getContent().size());
    }


    @Test
    void whenGetMyTickets_thenReturns200() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<TicketResponse> page = new PageImpl<>(List.of(ticketResponse));
        when(ticketService.getTicketsByUser(pageable)).thenReturn(page);

        ResponseEntity<Page<TicketResponse>> result = ticketController.getMyTickets(pageable);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }


    @Test
    void whenAssignAdmin_thenReturns200() {
        AssignAdminRequest request = new AssignAdminRequest(2L);
        when(ticketService.assignAdmin(1L, 2L)).thenReturn(ticketResponse);

        ResponseEntity<TicketResponse> result = ticketController.assignAdmin(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(ticketService, times(1)).assignAdmin(1L, 2L);
    }

    @Test
    void whenAssignAdmin_withNonExistentTicket_thenThrowsResourceNotFoundException() {
        AssignAdminRequest request = new AssignAdminRequest(2L);
        when(ticketService.assignAdmin(99L, 2L))
                .thenThrow(new ResourceNotFoundException("Ticket", "id", 99L));

        assertThrows(ResourceNotFoundException.class,
                () -> ticketController.assignAdmin(99L, request));
    }


    @Test
    void whenChangePriority_thenReturns200() {
        ChangePriorityRequest request = new ChangePriorityRequest(TicketPriority.HIGH);
        when(ticketService.changePriority(1L, TicketPriority.HIGH)).thenReturn(ticketResponse);

        ResponseEntity<TicketResponse> result = ticketController.changePriority(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }


    @Test
    void whenChangeStatus_thenReturns200() {
        ChangeStatusRequest request = new ChangeStatusRequest(TicketStatus.IN_PROGRESS);
        when(ticketService.changeStatus(1L, TicketStatus.IN_PROGRESS)).thenReturn(ticketResponse);

        ResponseEntity<TicketResponse> result = ticketController.changeStatus(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }


    @Test
    void whenCloseTicket_thenReturns200() {
        CloseTicketRequest request = new CloseTicketRequest("Ticket resuelto");
        when(ticketService.closeTicket(1L, "Ticket resuelto")).thenReturn(ticketResponse);

        ResponseEntity<TicketResponse> result = ticketController.closeTicket(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }


    @Test
    void whenReopenTicket_thenReturns200() {
        when(ticketService.reopenTicket(1L)).thenReturn(ticketResponse);

        ResponseEntity<TicketResponse> result = ticketController.reopenTicket(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void whenReopenTicket_withNonExistentTicket_thenThrowsResourceNotFoundException() {
        when(ticketService.reopenTicket(99L))
                .thenThrow(new ResourceNotFoundException("Ticket", "id", 99L));

        assertThrows(ResourceNotFoundException.class,
                () -> ticketController.reopenTicket(99L));
    }


    @Test
    void whenGetTicketById_thenReturns200() {
        when(ticketService.getTicketById(1L)).thenReturn(ticketResponse);

        ResponseEntity<TicketResponse> result = ticketController.getTicketById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1L, result.getBody().id());
    }

    @Test
    void whenGetTicketById_withNonExistentTicket_thenThrowsResourceNotFoundException() {
        when(ticketService.getTicketById(99L))
                .thenThrow(new ResourceNotFoundException("Ticket", "id", 99L));

        assertThrows(ResourceNotFoundException.class,
                () -> ticketController.getTicketById(99L));
    }
}