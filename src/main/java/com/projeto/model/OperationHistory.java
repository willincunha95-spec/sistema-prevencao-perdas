package com.projeto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "operation_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String operationType; // CREATE, UPDATE, DELETE, LOGIN

    @Column(nullable = false)
    private String entityName; // e.g., "Analysis", "User"

    @Column(nullable = false)
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User performedBy;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
