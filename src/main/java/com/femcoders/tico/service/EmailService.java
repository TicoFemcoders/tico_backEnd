package com.femcoders.tico.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(mailFrom);
    msg.setTo(toEmail);
    msg.setSubject("[TICO] Activa tu cuenta");
    msg.setText(
        "Hola " + userName + ",\n\n" +
            "Has sido invitado a TICO — plataforma de soporte de CoHispania.\n\n" +
            "Tu código de activación es:\n\n    " + code + "\n\n" +
            "Válido durante 30 minutos.\n\n" +
            "Accede a este enlace para activar tu cuenta:\n\n    " + link + "\n\n" +
            "Si no esperabas este email, ignóralo.\n\n— El equipo TICO");
    mailSender.send(msg);
  }

  public void sendResetEmail(String toEmail, String userName, String code) {
    String link = frontendUrl + "/reset-password?email=" + toEmail;
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(mailFrom);
    msg.setTo(toEmail);
    msg.setSubject("[TICO] Restablece tu contraseña");
    msg.setText(
        "Hola " + userName + ",\n\n" +
            "Recibimos una solicitud para restablecer la contraseña de tu cuenta en TICO.\n\n" +
            "Tu código de restablecimiento es:\n\n    " + code + "\n\n" +
            "Válido durante 30 minutos.\n\n" +
            "Accede a este enlace para introducir el código y elegir tu nueva contraseña:\n\n    " + link + "\n\n" +
            "Si no solicitaste este cambio, ignora este email — tu contraseña no cambiará.\n\n— El equipo TICO");
    mailSender.send(msg);
  }

  public void sendTicketCreatedEmail(String toEmail, String userName, String emailSubject) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(mailFrom);
    msg.setTo(toEmail);
    msg.setSubject(emailSubject);
    msg.setText(
        "Hola " + userName + ",\n\n" +
            "Tu ticket ha sido creado correctamente en TICO.\n\n" +
            "Referencia: " + emailSubject + "\n\n" +
            "Recibirás notificaciones en este mismo hilo cuando haya actualizaciones.\n\n" +
            "— El equipo TICO");
    mailSender.send(msg);
  }
}
