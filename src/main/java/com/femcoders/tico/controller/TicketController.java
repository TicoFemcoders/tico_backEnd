package com.femcoders.tico.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
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
import com.femcoders.tico.dto.response.TicketResponseDTO;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;
import com.femcoders.tico.service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketsService;

    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(
            @Valid @RequestBody TicketCreateReqDTO dto) {
        TicketResponseDTO response = ticketsService.createTicket(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        return new ResponseEntity<>(ticketsService.getAllTickets(), HttpStatus.OK);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets() {
        return new ResponseEntity<>(ticketsService.getTicketsByUser(), HttpStatus.OK);
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<TicketResponseDTO>> getAssignedTickets() {
        return new ResponseEntity<>(ticketsService.getTicketsByAdmin(), HttpStatus.OK);
    }

    @PutMapping("/{id}/assign-admin")
    public ResponseEntity<TicketResponseDTO> assignAdmin(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        return new ResponseEntity<>(ticketsService.assignAdmin(id, adminId), HttpStatus.OK);
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<TicketResponseDTO> changePriority(
            @PathVariable Long id,
            @RequestParam TicketPriority priority) {
        return new ResponseEntity<>(ticketsService.changePriority(id, priority), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TicketResponseDTO> changeStatus(
            @PathVariable Long id,
            @RequestParam TicketStatus status) {
        return new ResponseEntity<>(ticketsService.changeStatus(id, status), HttpStatus.OK);
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<TicketResponseDTO> closeTicket(
            @PathVariable Long id,
            @RequestParam(required = false) String closingMessage) {
        return new ResponseEntity<>(ticketsService.closeTicket(id, closingMessage), HttpStatus.OK);
    }

    @PutMapping("/{id}/reopen")
    public ResponseEntity<TicketResponseDTO> reopenTicket(@PathVariable Long id) {
        return new ResponseEntity<>(ticketsService.reopenTicket(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/labels/{labelId}")
    public ResponseEntity<TicketResponseDTO> assignLabel(
            @PathVariable Long id,
            @PathVariable Long labelId) {
        return new ResponseEntity<>(ticketsService.assignLabel(id, labelId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}/labels/{labelId}")
    public ResponseEntity<TicketResponseDTO> removeLabel(
            @PathVariable Long id,
            @PathVariable Long labelId) {
        return new ResponseEntity<>(ticketsService.removeLabel(id, labelId), HttpStatus.OK);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        return new ResponseEntity<>(ticketsService.getTicketById(id), HttpStatus.OK);
    }
}
