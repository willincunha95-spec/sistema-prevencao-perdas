package com.projeto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "error_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String errorCode;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userContext; // Can be null if system error not tied to user

    @Column(nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ErrorSeverity severity;

    public enum ErrorSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
