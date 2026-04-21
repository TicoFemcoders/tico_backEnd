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
    Map<String, Object> body = new HashMap<>();
    body.put("status", status.value());
    body.put("mensaje", mensaje);
    if (detalles != null) {
      body.put("errores", detalles);
    }
    return new ResponseEntity<>(body, status);
  }

}
