package com.femcoders.tico.utils;

import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;

public final class TicketStatusHelper {

  private TicketStatusHelper() {
  }

  public static String priorityToSpanish(TicketPriority priority) {
    return switch (priority) {
      case LOW -> "Baja";
      case MEDIUM -> "Media";
      case HIGH -> "Alta";
      case CRITICAL -> "Urgente";
    };
  }

  public static String statusToSpanish(TicketStatus status) {
    return switch (status) {
      case OPEN -> "Abierto";
      case IN_PROGRESS -> "En progreso";
      case CLOSED -> "Cerrado";
    };
  }
}
