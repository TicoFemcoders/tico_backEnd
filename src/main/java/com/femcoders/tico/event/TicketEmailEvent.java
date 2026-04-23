package com.femcoders.tico.event;

public record TicketEmailEvent(
        String type,
        String toEmail,
        String userName,
        String emailSubject,
        String extraParam
) {}
