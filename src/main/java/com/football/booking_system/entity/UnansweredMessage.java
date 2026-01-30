package com.football.booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "unanswered_messages")
public class UnansweredMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question; // Câu hỏi khách hàng hỏi

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public UnansweredMessage() {}
    public UnansweredMessage(String question) {
        this.question = question;
    }
}