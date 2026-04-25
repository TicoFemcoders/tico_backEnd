package com.femcoders.tico.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.request.TicketMessageRequest;
import com.femcoders.tico.dto.response.TicketMessageResponse;
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
    public ResponseEntity<Page<TicketMessageResponse>> getMessages(
            @PathVariable Long ticketId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ticketMessageService.getMessagesByTicketId(ticketId, pageable));
    }

    @PostMapping
    public ResponseEntity<TicketMessageResponse> createMessage(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketMessageRequest message) {
        TicketMessageResponse saved = ticketMessageService.createMessage(ticketId, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}
