package com.femcoders.tico.service;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import com.femcoders.tico.dto.request.TicketMessageRequest;
import com.femcoders.tico.dto.response.TicketMessageResponse;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.UserRole;
import com.femcoders.tico.exception.BadRequestException;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.mapper.TicketMessageMapper;
import com.femcoders.tico.repository.TicketMessageRepository;
import com.femcoders.tico.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TicketMessageServiceImplTest {

    @Mock private TicketMessageRepository ticketMessageRepository;
    @Mock private TicketMessageMapper ticketMessageMapper;
    @Mock private TicketRepository ticketRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private AuthService authService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private TicketMessageServiceImpl ticketMessageService;

    private User employee;
    private User admin;
    private Ticket ticket;
    private TicketMessage message;
    private TicketMessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setId(1L);
        employee.setName("Ana García");
        employee.setEmail("ana@test.com");
        employee.setRoles(Set.of(UserRole.EMPLOYEE));

        admin = new User();
        admin.setId(2L);
        admin.setName("Admin Test");
        admin.setEmail("admin@test.com");
        admin.setRoles(Set.of(UserRole.ADMIN));

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setEmailSubject("[TICO-10] Problema con impresora");
        ticket.setCreatedBy(employee);
        ticket.setAssignedTo(admin);

        message = new TicketMessage();
        message.setId(1L);
        message.setTicketId(10L);
        message.setContent("Hola, ¿puedes revisar esto?");
        message.setAuthor(employee);

        messageResponse = new TicketMessageResponse(1L, 10L, "Ana García",
                "Hola, ¿puedes revisar esto?", false, null, null);
    }

    // ── getMessagesByTicketId ──────────────────────────────────────────────

    @Test
    void getMessages_debeRetornarMensajes_cuandoAdminAccede() {
        Pageable pageable = PageRequest.of(0, 20);
        when(authService.getAuthenticatedUser()).thenReturn(admin);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketMessageRepository.findByTicketIdAndRecipientIdIsNullOrderByCreatedAtDesc(10L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(message)));
        when(ticketMessageMapper.toResponseDTO(message)).thenReturn(messageResponse);

        Page<TicketMessageResponse> result = ticketMessageService.getMessagesByTicketId(10L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getMessages_debeRetornarMensajes_cuandoCreadorAccede() {
        Pageable pageable = PageRequest.of(0, 20);
        when(authService.getAuthenticatedUser()).thenReturn(employee);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketMessageRepository.findByTicketIdAndRecipientIdIsNullOrderByCreatedAtDesc(10L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(message)));
        when(ticketMessageMapper.toResponseDTO(message)).thenReturn(messageResponse);

        Page<TicketMessageResponse> result = ticketMessageService.getMessagesByTicketId(10L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getMessages_debeLanzarExcepcion_cuandoEmployeeAccedeATicketAjeno() {
        User otroEmployee = new User();
        otroEmployee.setId(99L);
        otroEmployee.setRoles(Set.of(UserRole.EMPLOYEE));

        Pageable pageable = PageRequest.of(0, 20);
        when(authService.getAuthenticatedUser()).thenReturn(otroEmployee);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(AccessDeniedException.class,
                () -> ticketMessageService.getMessagesByTicketId(10L, pageable));
    }

    @Test
    void getMessages_debeLanzarExcepcion_cuandoTicketNoExiste() {
        Pageable pageable = PageRequest.of(0, 20);
        when(authService.getAuthenticatedUser()).thenReturn(admin);
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ticketMessageService.getMessagesByTicketId(99L, pageable));
    }

    // ── createMessage ──────────────────────────────────────────────────────

    @Test
    void createMessage_debeGuardarMensaje_cuandoCreadorEscribe() {
        TicketMessageRequest dto = new TicketMessageRequest(10L, "Nuevo mensaje", null);
        when(authService.getAuthenticatedUser()).thenReturn(employee);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketMessageMapper.toEntity(dto)).thenReturn(message);
        when(ticketMessageRepository.save(any())).thenReturn(message);
        when(ticketMessageMapper.toResponseDTO(message)).thenReturn(messageResponse);

        TicketMessageResponse result = ticketMessageService.createMessage(10L, dto);

        assertNotNull(result);
        verify(ticketMessageRepository).save(any(TicketMessage.class));
    }

    @Test
    void createMessage_debeEnviarNotificacion_cuandoAdminAsignadoEscribe() {
        TicketMessageRequest dto = new TicketMessageRequest(10L, "Respuesta del admin", null);
        when(authService.getAuthenticatedUser()).thenReturn(admin);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketMessageMapper.toEntity(dto)).thenReturn(message);
        when(ticketMessageRepository.save(any())).thenReturn(message);
        when(ticketMessageMapper.toResponseDTO(message)).thenReturn(messageResponse);

        ticketMessageService.createMessage(10L, dto);

        verify(notificationService).create(eq(10L), eq(admin), eq(1L), anyString());
    }

    @Test
    void createMessage_debeLanzarExcepcion_cuandoEmployeeEscribeEnTicketAjeno() {
        User otroEmployee = new User();
        otroEmployee.setId(99L);
        otroEmployee.setRoles(Set.of(UserRole.EMPLOYEE));

        TicketMessageRequest dto = new TicketMessageRequest(10L, "Mensaje", null);
        when(authService.getAuthenticatedUser()).thenReturn(otroEmployee);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(AccessDeniedException.class,
                () -> ticketMessageService.createMessage(10L, dto));
    }

    @Test
    void createMessage_debeLanzarExcepcion_cuandoAdminNoAsignadoEscribe() {
        User otroAdmin = new User();
        otroAdmin.setId(99L);
        otroAdmin.setRoles(Set.of(UserRole.ADMIN));

        TicketMessageRequest dto = new TicketMessageRequest(10L, "Mensaje", null);
        when(authService.getAuthenticatedUser()).thenReturn(otroAdmin);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class,
                () -> ticketMessageService.createMessage(10L, dto));
    }

    // ── deleteMessage ──────────────────────────────────────────────────────

    @Test
    void deleteMessage_debeEliminarMensaje() {
        ticketMessageService.deleteMessage(1L);

        verify(ticketMessageRepository).deleteById(1L);
    }
}