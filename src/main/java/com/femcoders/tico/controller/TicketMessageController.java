package com.femcoders.tico.controller;

import java.util.List;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.entity.TicketMessage;
import com.femcoders.tico.service.TicketMessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets/{ticketId}/messages")
public class TicketMessageController {

    private final TicketMessageService ticketMessageService;

    public TicketMessageController(TicketMessageService ticketMessageService) {
        this.ticketMessageService = ticketMessageService;
    }

    @GetMapping
    public ResponseEntity<List<TicketMessage>> getMessages(@PathVariable Long ticketId) {
        List<TicketMessage> messages = ticketMessageService.getMessagesByTicketId(ticketId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<TicketMessage> createMessage(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketMessage message) {
        message.setTicketId(ticketId);
        TicketMessage saved = ticketMessageService.createMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        ticketMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
