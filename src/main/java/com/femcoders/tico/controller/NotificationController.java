package com.femcoders.tico.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.femcoders.tico.dto.response.NotificationResponseDTO;
import com.femcoders.tico.dto.response.NotificationSummaryDTO;
import com.femcoders.tico.service.NotificationService;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("api/notifications")
public class NotificationController {

  @Autowired
  private NotificationService notificationService;

  @GetMapping
  public ResponseEntity<NotificationSummaryDTO> getPaginatedNotifications(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(notificationService.getPaginatedSummary(page, size));
  }

  @GetMapping("/unread")
  public ResponseEntity<List<NotificationResponseDTO>> getUnread() {
    return ResponseEntity.ok(notificationService.getUnread());
  }

  @PutMapping("/{id}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
    notificationService.markAsRead(id);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead() {
    notificationService.markAllAsRead();
    return ResponseEntity.ok().build();
  }

}
