package com.femcoders.tico.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.service.event.TicketCreatedEvent;
import com.femcoders.tico.service.event.TicketEmailEvent;
import com.femcoders.tico.utils.EmailTemplateBuilder;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTemplateBuilder tmpl;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@tico.com");
    }

    @Nested
    class SendActivationEmail {

        @Test
        void happyPath_sendsEmailWithActivationSubject() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.code(anyString())).thenReturn("<p>CODE</p>");
            when(tmpl.activationLink(anyString())).thenReturn("http://localhost/activation");

            emailService.sendActivationEmail("user@test.com", "Ana", "123456");

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo("[TICO] Activa tu cuenta");
        }

        @Test
        void sadPath_messagingExceptionIsWrappedInRuntimeException() throws MessagingException {
            MimeMessage mockMsg = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mockMsg);
            doThrow(new MessagingException("forced error")).when(mockMsg).setContent(any(Multipart.class));

            assertThatThrownBy(() -> emailService.sendActivationEmail("user@test.com", "Ana", "123456"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al enviar email");
        }
    }

    @Nested
    class SendResetEmail {

        @Test
        void happyPath_sendsEmailWithResetSubject() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.code(anyString())).thenReturn("<p>CODE</p>");
            when(tmpl.resetLink(anyString())).thenReturn("http://localhost/reset");

            emailService.sendResetEmail("user@test.com", "Ana", "654321");

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo("[TICO] Restablece tu contraseña");
        }

        @Test
        void sadPath_messagingExceptionIsWrappedInRuntimeException() throws MessagingException {
            MimeMessage mockMsg = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mockMsg);
            doThrow(new MessagingException("forced error")).when(mockMsg).setContent(any(Multipart.class));

            assertThatThrownBy(() -> emailService.sendResetEmail("user@test.com", "Ana", "654321"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al enviar email");
        }
    }

    @Nested
    class SendTicketCreatedEmail {

        @Test
        void happyPath_sendsEmailWithTicketSubject() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.cta(anyString())).thenReturn("<a>Ir a TICO</a>");

            emailService.sendTicketCreatedEmail("user@test.com", "Ana", "[TICO-5] Problema VPN");

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo("[TICO-5] Problema VPN");
        }

        @Test
        void sadPath_messagingExceptionIsWrappedInRuntimeException() throws MessagingException {
            MimeMessage mockMsg = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mockMsg);
            doThrow(new MessagingException("forced error")).when(mockMsg).setContent(any(Multipart.class));

            assertThatThrownBy(
                    () -> emailService.sendTicketCreatedEmail("user@test.com", "Ana", "[TICO-5] Problema VPN"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al enviar email");
        }
    }

    @Nested
    class SendPriorityChangedEmail {

        @Test
        void happyPath_sendsEmailWithTicketSubject() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.cta(anyString())).thenReturn("<a>Ir a TICO</a>");

            emailService.sendPriorityChangedEmail("user@test.com", "Ana", "[TICO-5] Problema VPN", "HIGH");

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo("[TICO-5] Problema VPN");
        }
    }

    @Nested
    class SendStatusChangedEmail {

        @Test
        void happyPath_sendsEmailWithTicketSubject() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.cta(anyString())).thenReturn("<a>Ir a TICO</a>");

            emailService.sendStatusChangedEmail("user@test.com", "Ana", "[TICO-5] Problema VPN", "IN_PROGRESS");

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo("[TICO-5] Problema VPN");
        }
    }

    @Nested
    class SendTicketClosedEmail {

        @Test
        void happyPath_sendsEmailWithTicketSubject() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.cta(anyString())).thenReturn("<a>Ir a TICO</a>");

            emailService.sendTicketClosedEmail("user@test.com", "Ana", "[TICO-5] Problema VPN");

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo("[TICO-5] Problema VPN");
        }
    }

    @Nested
    class SendNewMessageEmail {

        @Test
        void happyPath_sendsEmailWithTicketSubject() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.cta(anyString())).thenReturn("<a>Ir a TICO</a>");

            emailService.sendNewMessageEmail("user@test.com", "Ana", "[TICO-5] Problema VPN",
                    "Por favor reinicia el equipo.");

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo("[TICO-5] Problema VPN");
        }
    }

    @Nested
    class SendTicketReopenedEmail {

        @Test
        void happyPath_sendsEmailWithTicketSubject() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.cta(anyString())).thenReturn("<a>Ir a TICO</a>");

            emailService.sendTicketReopenedEmail("user@test.com", "Ana", "[TICO-5] Problema VPN");

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo("[TICO-5] Problema VPN");
        }
    }

    @Nested
    class OnTicketCreated {

        private User creator;
        private Ticket ticket;

        @BeforeEach
        void setUp() {
            creator = new User();
            creator.setEmail("creator@test.com");
            creator.setName("Creator");

            ticket = new Ticket();
            ticket.setId(5L);
            ticket.setCreatedBy(creator);
            ticket.setEmailSubject("[TICO-5] Problema VPN");
        }

        @Test
        void happyPath_sendsEmailToTicketCreator() throws MessagingException {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.cta(anyString())).thenReturn("<a>Ir a TICO</a>");

            emailService.onTicketCreated(new TicketCreatedEvent(ticket));

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo("[TICO-5] Problema VPN");
        }

        @Test
        void sadPath_exceptionIsSwallowedAndDoesNotPropagate() {
            assertThatNoException().isThrownBy(
                    () -> emailService.onTicketCreated(new TicketCreatedEvent(ticket)));
        }
    }

    @Nested
    class OnTicketEmail {

        private static final String TO_EMAIL = "user@test.com";
        private static final String USER_NAME = "Ana";
        private static final String EMAIL_SUBJECT = "[TICO-5] Problema VPN";

        private void stubForSend() {
            when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
            when(tmpl.wrap(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(tmpl.cta(anyString())).thenReturn("<a>Ir a TICO</a>");
        }

        @Test
        void priorityChanged_sendsEmail() throws MessagingException {
            stubForSend();
            emailService.onTicketEmail(
                    new TicketEmailEvent("PRIORITY_CHANGED", TO_EMAIL, USER_NAME, EMAIL_SUBJECT, "HIGH"));

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo(EMAIL_SUBJECT);
        }

        @Test
        void statusChanged_sendsEmail() throws MessagingException {
            stubForSend();
            emailService.onTicketEmail(
                    new TicketEmailEvent("STATUS_CHANGED", TO_EMAIL, USER_NAME, EMAIL_SUBJECT, "IN_PROGRESS"));

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo(EMAIL_SUBJECT);
        }

        @Test
        void closed_sendsEmail() throws MessagingException {
            stubForSend();
            emailService.onTicketEmail(new TicketEmailEvent("CLOSED", TO_EMAIL, USER_NAME, EMAIL_SUBJECT, null));

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo(EMAIL_SUBJECT);
        }

        @Test
        void reopened_sendsEmail() throws MessagingException {
            stubForSend();
            emailService.onTicketEmail(new TicketEmailEvent("REOPENED", TO_EMAIL, USER_NAME, EMAIL_SUBJECT, null));

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo(EMAIL_SUBJECT);
        }

        @Test
        void newMessage_sendsEmail() throws MessagingException {
            stubForSend();
            emailService.onTicketEmail(
                    new TicketEmailEvent("NEW_MESSAGE", TO_EMAIL, USER_NAME, EMAIL_SUBJECT, "Por favor reinicia."));

            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());
            assertThat(captor.getValue().getSubject()).isEqualTo(EMAIL_SUBJECT);
        }

        @Test
        void unknownType_doesNotThrowAndDoesNotSendEmail() {
            assertThatNoException().isThrownBy(
                    () -> emailService.onTicketEmail(
                            new TicketEmailEvent("UNKNOWN_TYPE", TO_EMAIL, USER_NAME, EMAIL_SUBJECT, null)));

            verify(mailSender, never()).send(any(MimeMessage.class));
        }

        @Test
        void sadPath_exceptionIsSwallowedAndDoesNotPropagate() {
            assertThatNoException().isThrownBy(
                    () -> emailService
                            .onTicketEmail(new TicketEmailEvent("CLOSED", TO_EMAIL, USER_NAME, EMAIL_SUBJECT, null)));
        }
    }
}
