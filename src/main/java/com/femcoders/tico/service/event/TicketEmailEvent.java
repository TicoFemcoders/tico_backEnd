package com.femcoders.tico.service.event;

public record TicketEmailEvent(
                String type,
                String toEmail,
                String userName,
                String emailSubject,
                String extraParam) {
}
