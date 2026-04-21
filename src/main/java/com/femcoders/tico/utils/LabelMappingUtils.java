package com.femcoders.tico.utils;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import com.femcoders.tico.entity.Label;
import com.femcoders.tico.enums.TicketStatus;

@Component
@Named("LabelMappingUtils")
public class LabelMappingUtils {

  @Named("countActiveTickets")
  public long countActive(Label label) {
    return label.getTickets().stream()
        .filter(t -> t.getStatus() != TicketStatus.CLOSED)
        .count();
  }

  @Named("countClosedTickets")
  public long countClosed(Label label) {
    return label.getTickets().stream()
        .filter(t -> t.getStatus() == TicketStatus.CLOSED)
        .count();
  }

}
