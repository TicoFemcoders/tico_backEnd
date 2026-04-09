package com.femcoders.tico.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.request.TicketCreateReqDTO;
import com.femcoders.tico.dto.response.TicketsResponseDTO;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.service.TicketsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/tickets")
public class TicketsController {

    @Autowired
    private TicketsService ticketsService;

    @PostMapping
    public ResponseEntity<TicketsResponseDTO> createTicket(
            @Valid @RequestBody TicketCreateReqDTO dto,
            @RequestParam Long userId) {
        TicketsResponseDTO response = ticketsService.createTicket(dto, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketsResponseDTO>> getAllTickets() {
        return new ResponseEntity<>(ticketsService.getAllTickets(), HttpStatus.OK);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketsResponseDTO>> getMyTickets(@RequestParam Long userId) {
        return new ResponseEntity<>(ticketsService.getTicketsByUser(userId), HttpStatus.OK);
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<TicketsResponseDTO>> getAssignedTickets(@RequestParam Long adminId) {
        return new ResponseEntity<>(ticketsService.getTicketsByAdmin(adminId), HttpStatus.OK);
    }

    @PutMapping("/{id}/assign-admin")
    public ResponseEntity<TicketsResponseDTO> assignAdmin(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        return new ResponseEntity<>(ticketsService.assignAdmin(id, adminId), HttpStatus.OK);
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<TicketsResponseDTO> changePriority(
            @PathVariable Long id,
            @RequestParam TicketPriority priority) {
        return new ResponseEntity<>(ticketsService.changePriority(id, priority), HttpStatus.OK);
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<TicketsResponseDTO> closeTicket(@PathVariable Long id) {
        return new ResponseEntity<>(ticketsService.closeTicket(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/labels/{labelId}")
    public ResponseEntity<TicketsResponseDTO> assignLabel(
            @PathVariable Long id,
            @PathVariable Long labelId) {
        return new ResponseEntity<>(ticketsService.assignLabel(id, labelId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}/labels/{labelId}")
    public ResponseEntity<TicketsResponseDTO> removeLabel(
            @PathVariable Long id,
            @PathVariable Long labelId) {
        return new ResponseEntity<>(ticketsService.removeLabel(id, labelId), HttpStatus.OK);
    }
}
