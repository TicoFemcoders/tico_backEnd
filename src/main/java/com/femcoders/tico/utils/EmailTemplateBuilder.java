package com.femcoders.tico.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateBuilder {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public String wrap(String bodyContent) {
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

    public String cta(String label) {
        return "<a href='" + frontendUrl + "' style='display:inline-block;margin-top:24px;" +
                "padding:12px 28px;background:#f28a2e;color:#ffffff;text-decoration:none;" +
                "border-radius:6px;font-weight:600;font-size:15px;'>" + label + "</a>";
    }

    public String activationLink(String toEmail) {
        return frontendUrl + "/activation?email=" + toEmail;
    }

    public String resetLink(String toEmail) {
        return frontendUrl + "/reset-password?email=" + toEmail;
    }

    public String code(String code) {
        return "<p style='font-size:32px;font-weight:700;letter-spacing:6px;color:#202B45;" +
                "background:#f4f4f4;padding:12px 24px;border-radius:6px;display:inline-block;'>" +
                code + "</p>" +
                "<p style='color:#888;font-size:13px;'>Válido durante 30 minutos.</p>";
    }
}
