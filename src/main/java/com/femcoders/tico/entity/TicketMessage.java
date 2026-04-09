package com.femcoders.tico.entity;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;





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
