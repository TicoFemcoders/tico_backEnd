package com.femcoders.tico.service.event;

import com.femcoders.tico.entity.Ticket;

public record TicketCreatedEvent(Ticket ticket) {
}
