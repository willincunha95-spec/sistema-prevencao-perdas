package com.projeto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Register {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column
    private String color;

    @Column
    private String location;

    @Column
    private String sku;

    @Column
    private String floor;

    @Column
    private String street;

    @Column
    private String position;

    @Column
    private String subPosition;

    @Column
    private String identifierId;

    @Column
    private String sellerId;

    @Column
    private String meliId;

    @Column
    private java.math.BigDecimal salePrice;

    @Column
    private java.math.BigDecimal compensationPrice;

    @Column(nullable = false)
    private String protocol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegisterType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PrePersist
    public void prePersist() {
        if (this.protocol == null || this.protocol.isEmpty()) {
            this.protocol = java.util.UUID.randomUUID().toString();
        }
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User registeredBy;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private RegisterStatus status = RegisterStatus.PENDING;

    public enum RegisterType {
        ENTRY, EXIT, TRANSFER, ADJUSTMENT
    }

    public enum RegisterStatus {
        PENDING, ANALYZED
    }
}
