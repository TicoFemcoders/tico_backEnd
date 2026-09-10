package com.femcoders.tico.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ExceptionResponseBuilder {
  public ResponseEntity<Map<String, Object>> build(
      HttpStatus status, String mensaje, Object detalles) {
    return build(status, mensaje, detalles, null);
  }

  /**
   * @param code código de error opcional para que el frontend distinga
   *             programáticamente entre variantes de un mismo status (p. ej.
   *             un 403 por rol/sesión caducada frente a un 403 por una regla
   *             de negocio puntual) sin tener que parsear el mensaje.
   */
  public ResponseEntity<Map<String, Object>> build(
      HttpStatus status, String mensaje, Object detalles, String code) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", status.value());
    body.put("mensaje", mensaje);
    if (detalles != null) {
      body.put("errores", detalles);
    }
    if (code != null) {
      body.put("code", code);
    }
    return new ResponseEntity<>(body, status);
  }

}
