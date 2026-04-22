package com.femcoders.tico.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TicketResponseDTO>> getAllTickets(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketsService.getAllTickets(pageable));
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<Page<TicketResponseDTO>> getMyTickets(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketsService.getTicketsByUser(pageable));
    }

    @GetMapping("/assigned")
    public ResponseEntity<Page<TicketResponseDTO>> getAssignedTickets(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketsService.getTicketsByAdmin(pageable));
    }

    @PutMapping("/{id}/assign-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponseDTO> assignAdmin(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        return new ResponseEntity<>(ticketsService.assignAdmin(id, adminId), HttpStatus.OK);
    }

    @PutMapping("/{id}/priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponseDTO> changePriority(
            @PathVariable Long id,
            @RequestParam TicketPriority priority) {
        return new ResponseEntity<>(ticketsService.changePriority(id, priority), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponseDTO> changeStatus(
            @PathVariable Long id,
            @RequestParam TicketStatus status) {
        return new ResponseEntity<>(ticketsService.changeStatus(id, status), HttpStatus.OK);
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponseDTO> assignLabel(
            @PathVariable Long id,
            @PathVariable Long labelId) {
        return new ResponseEntity<>(ticketsService.assignLabel(id, labelId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}/labels/{labelId}")
    @PreAuthorize("hasRole('ADMIN')")
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
