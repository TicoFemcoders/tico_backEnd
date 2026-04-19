package com.femcoders.tico.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Value("${app.mail.from}")
  private String mailFrom;

  @Value("${app.frontend.url}")
  private String frontendUrl;

  public void sendActivationEmail(String toEmail, String userName, String code) {
    String link = frontendUrl + "/activation?email=" + toEmail;
    String body = "<p>Hola <strong>" + userName + "</strong>,</p>" +
        "<p>Has sido invitado a <strong>TICO</strong> — plataforma de soporte de CoHispania.</p>" +
        "<p style='margin-top:24px;'>Tu código de activación es:</p>" +
        "<p style='font-size:32px;font-weight:700;letter-spacing:6px;color:#202B45;" +
        "background:#f4f4f4;padding:12px 24px;border-radius:6px;display:inline-block;'>" +
        code + "</p>" +
        "<p style='color:#888;font-size:13px;'>Válido durante 30 minutos.</p>" +
        "<a href='" + link + "' style='display:inline-block;margin-top:16px;" +
        "padding:12px 28px;background:#f28a2e;color:#ffffff;text-decoration:none;" +
        "border-radius:6px;font-weight:600;font-size:15px;'>Activar cuenta</a>" +
        "<p style='margin-top:32px;color:#aaa;font-size:12px;'>" +
        "Si no esperabas este email, ignóralo.</p>";
    send(toEmail, "[TICO] Activa tu cuenta", body);
  }

  public void sendResetEmail(String toEmail, String userName, String code) {
    String link = frontendUrl + "/reset-password?email=" + toEmail;
    String body = "<p>Hola <strong>" + userName + "</strong>,</p>" +
        "<p>Recibimos una solicitud para restablecer la contraseña de tu cuenta en TICO.</p>" +
        "<p style='margin-top:24px;'>Tu código de restablecimiento es:</p>" +
        "<p style='font-size:32px;font-weight:700;letter-spacing:6px;color:#202B45;" +
        "background:#f4f4f4;padding:12px 24px;border-radius:6px;display:inline-block;'>" +
        code + "</p>" +
        "<p style='color:#888;font-size:13px;'>Válido durante 30 minutos.</p>" +
        "<a href='" + link + "' style='display:inline-block;margin-top:16px;" +
        "padding:12px 28px;background:#f28a2e;color:#ffffff;text-decoration:none;" +
        "border-radius:6px;font-weight:600;font-size:15px;'>Restablecer contraseña</a>" +
        "<p style='margin-top:48px;color:#aaa;font-size:12px;'>" +
        "Si no solicitaste este cambio, ignora este email — tu contraseña no cambiará.</p>";
    send(toEmail, "[TICO] Restablece tu contraseña", body);
  }

  public void sendTicketCreatedEmail(String toEmail, String userName, String emailSubject) {
    String body = "<p>Hola <strong>" + userName + "</strong>,</p>" +
        "<p>Tu ticket ha sido creado correctamente en TICO.</p>" +
        "<p><strong>Referencia:</strong> " + emailSubject + "</p>" +
        "<p>Recibirás notificaciones en este mismo hilo cuando haya actualizaciones.</p>";
    send(toEmail, emailSubject, body);
  }

  public void sendPriorityChangedEmail(String toEmail, String userName, String emailSubject, String newPriority) {
    String body = "<p>Hola <strong>" + userName + "</strong>,</p>" +
        "<p>La prioridad de tu ticket ha sido actualizada.</p>" +
        "<p><strong>Ticket:</strong> " + emailSubject + "</p>" +
        "<p><strong>Nueva prioridad:</strong> " + newPriority + "</p>";
    send(toEmail, emailSubject, body);
  }

  public void sendStatusChangedEmail(String toEmail, String userName, String emailSubject, String newStatus) {
    String body = "<p>Hola <strong>" + userName + "</strong>,</p>" +
        "<p>El estado de tu ticket ha sido actualizado.</p>" +
        "<p><strong>Ticket:</strong> " + emailSubject + "</p>" +
        "<p><strong>Nuevo estado:</strong> " + newStatus + "</p>";
    send(toEmail, emailSubject, body);
  }

  public void sendTicketClosedEmail(String toEmail, String userName, String emailSubject) {
    String body = "<p>Hola <strong>" + userName + "</strong>,</p>" +
        "<p>Tu ticket ha sido cerrado.</p>" +
        "<p><strong>Ticket:</strong> " + emailSubject + "</p>" +
        "<p>Si necesitas reabrir el ticket puedes hacerlo desde la plataforma.</p>";
    send(toEmail, emailSubject, body);
  }

  private void send(String toEmail, String subject, String bodyHtml) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(mailFrom);
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(buildHtml(bodyHtml), true);
      helper.addInline("logo", new ClassPathResource("images/logoTico.png"));
      mailSender.send(message);
    } catch (MessagingException e) {
      throw new RuntimeException("Error al enviar email a " + toEmail, e);
    }
  }

  private String buildHtml(String bodyContent) {
    return "<!DOCTYPE html><html lang='es'><body style='margin:0;padding:0;" +
        "background:#f4f4f4;font-family:Arial,Helvetica,sans-serif;'>" +
        "<table width='600' cellpadding='0' cellspacing='0' border='0' " +
        "style='margin:32px auto;background:#ffffff;border-radius:8px;" +
        "overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);'>" +

        "<tr><td style='background:#202B45;padding:24px 32px;'>" +
        "<img src='cid:logo' alt='TICO' height='70' style='display:block;'>" +
        "</td></tr>" +

        "<tr><td style='padding:32px;font-size:15px;color:#333333;line-height:1.7;text-align:center;'>" +
        bodyContent +
        "</td></tr>" +

        "<tr><td style='background:#f9f9f9;padding:16px 32px;font-size:12px;" +
        "color:#aaaaaa;text-align:center;border-top:1px solid #eeeeee;'>" +
        "TICO &mdash; Sistema de soporte de CoHispania" +
        "</td></tr>" +
        "</table></body></html>";
  }
}