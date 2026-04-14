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

  public void sendActivationCode(String toEmail, String userName, String code) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(mailFrom);
    msg.setTo(toEmail);
    msg.setSubject("[TICO] Tu código de activación");
    msg.setText(
        "Hola " + userName + ",\n\n" +
            "Has sido invitado a TICO — plataforma de soporte de CoHispania.\n\n" +
            "Tu código de activación es:\n\n    " + code + "\n\n" +
            "Válido durante 30 minutos.\n" +
            "Si no esperabas este email, ignóralo.\n\n— El equipo TICO");
    mailSender.send(msg);
  }

  public void sendActivationLink(String toEmail, String userName) {
    String link = frontendUrl + "/activation?email=" + toEmail;
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(mailFrom);
    msg.setTo(toEmail);
    msg.setSubject("[TICO] Activa tu cuenta");
    msg.setText(
        "Hola " + userName + ",\n\n" +
            "Accede a este enlace para activar tu cuenta:\n\n" + link + "\n\n" +
            "Necesitarás el código que te hemos enviado en un email separado.\n\n" +
            "— El equipo TICO");
    mailSender.send(msg);
  }
}
