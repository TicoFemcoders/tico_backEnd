package com.femcoders.tico.controller;

import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.service.TicketMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets/{ticketId}/messages")
public class TicketMessageController {

    private final TicketMessageService ticketMessageService;

    public TicketMessageController(TicketMessageService ticketMessageService) {
        this.ticketMessageService = ticketMessageService;
    }

    @GetMapping
    public ResponseEntity<List<TicketMessage>> getMessages(@PathVariable UUID ticketId) {
        List<TicketMessage> messages = ticketMessageService.getMessagesByTicketId(ticketId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<TicketMessage> createMessage(
            @PathVariable UUID ticketId,
            @RequestBody TicketMessage message) {
        message.setTicketId(ticketId);
        TicketMessage saved = ticketMessageService.createMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID id) {
        ticketMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
