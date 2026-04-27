package com.femcoders.tico.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.request.AssignAdminRequest;
import com.femcoders.tico.dto.request.ChangePriorityRequest;
import com.femcoders.tico.dto.request.ChangeStatusRequest;
import com.femcoders.tico.dto.request.CloseTicketRequest;
import com.femcoders.tico.dto.request.TicketCreateRequest;
import com.femcoders.tico.dto.response.TicketResponse;
import com.femcoders.tico.service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketsService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketCreateRequest dto) {
        TicketResponse response = ticketsService.createTicket(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TicketResponse>> getAllTickets(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ticketsService.getAllTickets(pageable));
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<Page<TicketResponse>> getMyTickets(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ticketsService.getTicketsByUser(pageable));
    }

    @GetMapping("/assigned")
    public ResponseEntity<Page<TicketResponse>> getAssignedTickets(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ticketsService.getTicketsByAdmin(pageable));
    }

    @PatchMapping("/{id}/assign-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> assignAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AssignAdminRequest dto) {
        return new ResponseEntity<>(ticketsService.assignAdmin(id, dto.adminId()), HttpStatus.OK);
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> changePriority(
            @PathVariable Long id,
            @Valid @RequestBody ChangePriorityRequest dto) {
        return new ResponseEntity<>(ticketsService.changePriority(id, dto.priority()), HttpStatus.OK);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest dto) {
        return new ResponseEntity<>(ticketsService.changeStatus(id, dto.status()), HttpStatus.OK);
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> closeTicket(
            @PathVariable Long id,
            @Valid @RequestBody CloseTicketRequest dto) {
        return new ResponseEntity<>(ticketsService.closeTicket(id, dto.closingMessage()), HttpStatus.OK);
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<TicketResponse> reopenTicket(@PathVariable Long id) {
        return new ResponseEntity<>(ticketsService.reopenTicket(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/labels/{labelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> assignLabel(
            @PathVariable Long id,
            @PathVariable Long labelId) {
        return new ResponseEntity<>(ticketsService.assignLabel(id, labelId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}/labels/{labelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> removeLabel(
            @PathVariable Long id,
            @PathVariable Long labelId) {
        return new ResponseEntity<>(ticketsService.removeLabel(id, labelId), HttpStatus.OK);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return new ResponseEntity<>(ticketsService.getTicketById(id), HttpStatus.OK);
    }
}
