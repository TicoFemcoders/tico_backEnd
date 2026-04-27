package com.femcoders.tico.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import com.femcoders.tico.service.event.TicketCreatedEvent;
import com.femcoders.tico.service.event.TicketEmailEvent;
import com.femcoders.tico.utils.EmailTemplateBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder tmpl;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketCreated(TicketCreatedEvent event) {
        try {
            sendTicketCreatedEmail(
                    event.ticket().getCreatedBy().getEmail(),
                    event.ticket().getCreatedBy().getName(),
                    event.ticket().getEmailSubject());
        } catch (Exception ex) {
            log.error("Error enviando email de creación de ticket {}: {}", event.ticket().getId(), ex.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketEmail(TicketEmailEvent event) {
        try {
            switch (event.type()) {
                case "PRIORITY_CHANGED" -> sendPriorityChangedEmail(
                        event.toEmail(), event.userName(), event.emailSubject(), event.extraParam());
                case "STATUS_CHANGED" -> sendStatusChangedEmail(
                        event.toEmail(), event.userName(), event.emailSubject(), event.extraParam());
                case "CLOSED" -> sendTicketClosedEmail(
                        event.toEmail(), event.userName(), event.emailSubject());
                case "REOPENED" -> sendTicketReopenedEmail(
                        event.toEmail(), event.userName(), event.emailSubject());
                case "NEW_MESSAGE" -> sendNewMessageEmail(
                        event.toEmail(), event.userName(), event.emailSubject(), event.extraParam());
                default -> log.warn("Tipo de evento de email desconocido: {}", event.type());
            }
        } catch (Exception ex) {
            log.error("Error enviando email tipo {} a {}: {}", event.type(), event.toEmail(), ex.getMessage());
        }
    }

    public void sendActivationEmail(String toEmail, String userName, String code) {
        String safeUserName = HtmlUtils.htmlEscape(userName);
        String body = "<p>Hola <strong>" + safeUserName + "</strong>,</p>" +
                "<p>Has sido invitado a <strong>TICO</strong> — plataforma de soporte de CoHispania.</p>" +
                "<p style='margin-top:24px;'>Tu código de activación es:</p>" +
                tmpl.code(code) +
                "<a href='" + tmpl.activationLink(toEmail) + "' " +
                "style='display:inline-block;margin-top:16px;padding:12px 28px;" +
                "background:#f28a2e;color:#ffffff;text-decoration:none;" +
                "border-radius:6px;font-weight:600;font-size:15px;'>Activar cuenta</a>" +
                "<p style='margin-top:32px;color:#aaa;font-size:12px;'>Si no esperabas este email, ignóralo.</p>";
        send(toEmail, "[TICO] Activa tu cuenta", body);
    }

    public void sendResetEmail(String toEmail, String userName, String code) {
        String safeUserName = HtmlUtils.htmlEscape(userName);
        String body = "<p>Hola <strong>" + safeUserName + "</strong>,</p>" +
                "<p>Recibimos una solicitud para restablecer la contraseña de tu cuenta en TICO.</p>" +
                "<p style='margin-top:24px;'>Tu código de restablecimiento es:</p>" +
                tmpl.code(code) +
                "<a href='" + tmpl.resetLink(toEmail) + "' " +
                "style='display:inline-block;margin-top:16px;padding:12px 28px;" +
                "background:#f28a2e;color:#ffffff;text-decoration:none;" +
                "border-radius:6px;font-weight:600;font-size:15px;'>Restablecer contraseña</a>" +
                "<p style='margin-top:48px;color:#aaa;font-size:12px;'>" +
                "Si no solicitaste este cambio, ignora este email — tu contraseña no cambiará.</p>";
        send(toEmail, "[TICO] Restablece tu contraseña", body);
    }

    public void sendTicketCreatedEmail(String toEmail, String userName, String emailSubject) {
        String safeUserName = HtmlUtils.htmlEscape(userName);
        String safeSubject = HtmlUtils.htmlEscape(emailSubject);
        String body = "<p>Hola <strong>" + safeUserName + "</strong>,</p>" +
                "<p>Tu ticket ha sido creado correctamente en TICO.</p>" +
                "<p><strong>Referencia:</strong> " + safeSubject + "</p>" +
                "<p>Recibirás notificaciones en este mismo hilo cuando haya actualizaciones.</p>" +
                tmpl.cta("Ir a TICO");
        send(toEmail, emailSubject, body);
    }

    public void sendPriorityChangedEmail(String toEmail, String userName, String emailSubject, String newPriority) {
        String safeUserName = HtmlUtils.htmlEscape(userName);
        String safeSubject = HtmlUtils.htmlEscape(emailSubject);
        String safePriority = HtmlUtils.htmlEscape(newPriority);
        String body = "<p>Hola <strong>" + safeUserName + "</strong>,</p>" +
                "<p>La prioridad de tu ticket ha sido actualizada.</p>" +
                "<p><strong>Ticket:</strong> " + safeSubject + "</p>" +
                "<p><strong>Nueva prioridad:</strong> " + safePriority + "</p>" +
                tmpl.cta("Ir a TICO");
        send(toEmail, emailSubject, body);
    }

    public void sendStatusChangedEmail(String toEmail, String userName, String emailSubject, String newStatus) {
        String safeUserName = HtmlUtils.htmlEscape(userName);
        String safeSubject = HtmlUtils.htmlEscape(emailSubject);
        String safeStatus = HtmlUtils.htmlEscape(newStatus);
        String body = "<p>Hola <strong>" + safeUserName + "</strong>,</p>" +
                "<p>El estado de tu ticket ha sido actualizado.</p>" +
                "<p><strong>Ticket:</strong> " + safeSubject + "</p>" +
                "<p><strong>Nuevo estado:</strong> " + safeStatus + "</p>" +
                tmpl.cta("Ir a TICO");
        send(toEmail, emailSubject, body);
    }

    public void sendTicketClosedEmail(String toEmail, String userName, String emailSubject) {
        String safeUserName = HtmlUtils.htmlEscape(userName);
        String safeSubject = HtmlUtils.htmlEscape(emailSubject);
        String body = "<p>Hola <strong>" + safeUserName + "</strong>,</p>" +
                "<p>Tu ticket ha sido cerrado.</p>" +
                "<p><strong>Ticket:</strong> " + safeSubject + "</p>" +
                "<p>Si necesitas reabrir el ticket puedes hacerlo desde la plataforma.</p>" +
                tmpl.cta("Ir a TICO");
        send(toEmail, emailSubject, body);
    }

    public void sendNewMessageEmail(String toEmail, String userName, String emailSubject, String messageContent) {
        String safeUserName = HtmlUtils.htmlEscape(userName);
        String safeSubject = HtmlUtils.htmlEscape(emailSubject);
        String safeContent = HtmlUtils.htmlEscape(messageContent);
        String body = "<p>Hola <strong>" + safeUserName + "</strong>,</p>" +
                "<p>El equipo de soporte ha respondido a tu ticket.</p>" +
                "<p><strong>Ticket:</strong> " + safeSubject + "</p>" +
                "<p style='margin-top:16px;padding:12px 20px;background:#f9f9f9;border-left:3px solid #f28a2e;" +
                "border-radius:4px;font-style:italic;color:#555;text-align:center;'>" +
                "&ldquo;" + safeContent + "&rdquo;</p>" +
                tmpl.cta("Ir a TICO");
        send(toEmail, emailSubject, body);
    }

    public void sendTicketReopenedEmail(String toEmail, String userName, String emailSubject) {
        String safeUserName = HtmlUtils.htmlEscape(userName);
        String safeSubject = HtmlUtils.htmlEscape(emailSubject);
        String body = "<p>Hola <strong>" + safeUserName + "</strong>,</p>" +
                "<p>Tu ticket ha sido reactivado y está de nuevo en seguimiento.</p>" +
                "<p><strong>Ticket:</strong> " + safeSubject + "</p>" +
                "<p>El equipo de soporte revisará tu caso próximamente.</p>" +
                tmpl.cta("Ir a TICO");
        send(toEmail, emailSubject, body);
    }

    private void send(String toEmail, String subject, String bodyHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(tmpl.wrap(bodyHtml), true);
            helper.addInline("logo", new ClassPathResource("images/logoTico.png"));
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar email (destinatario enmascarado en logs)", e);
        }
    }
}
