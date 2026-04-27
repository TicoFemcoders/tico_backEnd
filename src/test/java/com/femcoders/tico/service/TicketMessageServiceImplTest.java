package com.femcoders.tico.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import com.femcoders.tico.service.event.TicketEmailEvent;

@ExtendWith(MockitoExtension.class)
class TicketMessageServiceImplTest {

    @Mock
    private TicketMessageRepository ticketMessageRepository;
    @Mock
    private TicketMessageMapper ticketMessageMapper;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private AuthService authService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TicketMessageServiceImpl service;

    private User employee;
    private User admin;
    private User otherEmployee;
    private User otherAdmin;
    private Ticket ticket;
    private TicketMessage message;
    private TicketMessageResponse response;
    private TicketMessageRequest request;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setId(1L);
        employee.setName("Employee User");
        employee.setEmail("employee@test.com");
        employee.setRoles(Set.of(UserRole.EMPLOYEE));

        admin = new User();
        admin.setId(2L);
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setRoles(Set.of(UserRole.ADMIN));

        otherEmployee = new User();
        otherEmployee.setId(3L);
        otherEmployee.setName("Other Employee");
        otherEmployee.setEmail("other.employee@test.com");
        otherEmployee.setRoles(Set.of(UserRole.EMPLOYEE));

        otherAdmin = new User();
        otherAdmin.setId(4L);
        otherAdmin.setName("Other Admin");
        otherAdmin.setEmail("other.admin@test.com");
        otherAdmin.setRoles(Set.of(UserRole.ADMIN));

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setTitle("Test Ticket");
        ticket.setEmailSubject("[TICO-10] Test Ticket");
        ticket.setCreatedBy(employee);
        ticket.setAssignedTo(admin);

        message = new TicketMessage();
        message.setId(100L);
        message.setTicketId(10L);
        message.setContent("Test content");
        message.setAuthor(employee);

        response = new TicketMessageResponse(100L, 10L, "Employee User", "Test content", false, null, null);
        request = new TicketMessageRequest(10L, "Test content", null);
        pageable = PageRequest.of(0, 10);
    }

    @Nested
    class GetMessagesByTicketId {

        @Test
        void adminUser_returnsPageOfMessages() {
            when(authService.getAuthenticatedUser()).thenReturn(admin);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
            Page<TicketMessage> page = new PageImpl<>(List.of(message));
            when(ticketMessageRepository
                    .findByTicketIdAndRecipientIdIsNullOrderByCreatedAtDesc(10L, pageable))
                    .thenReturn(page);
            when(ticketMessageMapper.toResponseDTO(message)).thenReturn(response);

            Page<TicketMessageResponse> result = service.getMessagesByTicketId(10L, pageable);

            assertThat(result.getContent()).containsExactly(response);
        }

        @Test
        void employeeWhoCreatedTicket_returnsPageOfMessages() {
            when(authService.getAuthenticatedUser()).thenReturn(employee);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
            Page<TicketMessage> page = new PageImpl<>(List.of(message));
            when(ticketMessageRepository
                    .findByTicketIdAndRecipientIdIsNullOrderByCreatedAtDesc(10L, pageable))
                    .thenReturn(page);
            when(ticketMessageMapper.toResponseDTO(message)).thenReturn(response);

            Page<TicketMessageResponse> result = service.getMessagesByTicketId(10L, pageable);

            assertThat(result.getContent()).containsExactly(response);
        }

        @Test
        void ticketNotFound_throwsResourceNotFoundException() {
            when(authService.getAuthenticatedUser()).thenReturn(employee);
            when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getMessagesByTicketId(99L, pageable))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void employeeNotTicketOwner_throwsAccessDeniedException() {
            when(authService.getAuthenticatedUser()).thenReturn(otherEmployee);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> service.getMessagesByTicketId(10L, pageable))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("No tienes acceso");
        }
    }

    @Nested
    class CreateMessage {

        @Test
        void assignedAdminReplies_publishesEmailEventAndNotifiesCreator() {
            when(authService.getAuthenticatedUser()).thenReturn(admin);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
            when(ticketMessageMapper.toEntity(request)).thenReturn(message);
            when(ticketMessageRepository.save(message)).thenReturn(message);
            when(ticketMessageMapper.toResponseDTO(message)).thenReturn(response);

            TicketMessageResponse result = service.createMessage(10L, request);

            assertThat(result).isEqualTo(response);

            ArgumentCaptor<TicketEmailEvent> eventCaptor = ArgumentCaptor.forClass(TicketEmailEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            TicketEmailEvent event = eventCaptor.getValue();
            assertThat(event.type()).isEqualTo("NEW_MESSAGE");
            assertThat(event.toEmail()).isEqualTo(employee.getEmail());
            assertThat(event.emailSubject()).isEqualTo(ticket.getEmailSubject());

            verify(notificationService).create(
                    eq(ticket.getId()), eq(admin), eq(employee.getId()), anyString());
        }

        @Test
        void assignedAdminRepliesButCreatorIsAdmin_noEmailEventButStillNotifiesCreator() {
            ticket.setCreatedBy(otherAdmin);
            when(authService.getAuthenticatedUser()).thenReturn(admin);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
            when(ticketMessageMapper.toEntity(request)).thenReturn(message);
            when(ticketMessageRepository.save(message)).thenReturn(message);
            when(ticketMessageMapper.toResponseDTO(message)).thenReturn(response);

            service.createMessage(10L, request);

            verify(eventPublisher, never()).publishEvent(any());
            verify(notificationService).create(
                    eq(ticket.getId()), eq(admin), eq(otherAdmin.getId()), anyString());
        }

        @Test
        void employeeCreatorRepliesWithAssignedAdmin_notifiesAssignedAdmin() {
            when(authService.getAuthenticatedUser()).thenReturn(employee);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
            when(ticketMessageMapper.toEntity(request)).thenReturn(message);
            when(ticketMessageRepository.save(message)).thenReturn(message);
            when(ticketMessageMapper.toResponseDTO(message)).thenReturn(response);

            service.createMessage(10L, request);

            verify(eventPublisher, never()).publishEvent(any());
            verify(notificationService).create(
                    eq(ticket.getId()), eq(employee), eq(admin.getId()), anyString());
        }

        @Test
        void employeeCreatorRepliesOnUnassignedTicket_noEmailNoNotification() {
            ticket.setAssignedTo(null);
            when(authService.getAuthenticatedUser()).thenReturn(employee);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
            when(ticketMessageMapper.toEntity(request)).thenReturn(message);
            when(ticketMessageRepository.save(message)).thenReturn(message);
            when(ticketMessageMapper.toResponseDTO(message)).thenReturn(response);

            TicketMessageResponse result = service.createMessage(10L, request);

            assertThat(result).isEqualTo(response);
            verify(eventPublisher, never()).publishEvent(any());
            verify(notificationService, never()).create(any(), any(), any(), any());
        }

        @Test
        void adminWritesOnUnassignedTicket_savesMessageNoSideEffects() {
            ticket.setAssignedTo(null);
            when(authService.getAuthenticatedUser()).thenReturn(admin);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
            when(ticketMessageMapper.toEntity(request)).thenReturn(message);
            when(ticketMessageRepository.save(message)).thenReturn(message);
            when(ticketMessageMapper.toResponseDTO(message)).thenReturn(response);

            TicketMessageResponse result = service.createMessage(10L, request);

            assertThat(result).isEqualTo(response);
            verify(ticketMessageRepository).save(message);
            verify(eventPublisher, never()).publishEvent(any());
            verify(notificationService, never()).create(any(), any(), any(), any());
        }

        @Test
        void ticketNotFound_throwsResourceNotFoundException() {
            when(authService.getAuthenticatedUser()).thenReturn(employee);
            when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createMessage(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(ticketMessageRepository, never()).save(any());
        }

        @Test
        void employeeNotTicketCreator_throwsAccessDeniedException() {
            when(authService.getAuthenticatedUser()).thenReturn(otherEmployee);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> service.createMessage(10L, request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Solo el creador del ticket");

            verify(ticketMessageRepository, never()).save(any());
        }

        @Test
        void adminNotAssignedToTicket_throwsBadRequestException() {
            when(authService.getAuthenticatedUser()).thenReturn(otherAdmin);
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> service.createMessage(10L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Solo el admin asignado");

            verify(ticketMessageRepository, never()).save(any());
        }
    }
}
