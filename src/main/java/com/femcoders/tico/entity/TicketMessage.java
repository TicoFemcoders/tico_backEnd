package com.femcoders.tico.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;





@Entity
@Table(name = "ticket_message")
@Data


public class TicketMessage {


   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   @Column(name = "id")
   private UUID id;

   @Column(name = "ticket_id", nullable = false)
   private UUID ticketId;

   @Column(name = "author_id", nullable = false)
   private UUID authorId;

   @Column(name = "content", nullable = false, columnDefinition = "TEXT")
   private String content;


   @Column(name = "is_internal", nullable = false)
   private Boolean isInternal = false;


   @Column(name = "created_at", updatable = false)
   private LocalDateTime createdAt;

   @PrePersist
   protected void onCreate() {
      this.createdAt = LocalDateTime.now();




    
}
    


}
