package com.projeto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "dfls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DFL {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column
    private String color;

    @Column
    private String location;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitValue;

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

    @Column(columnDefinition = "TEXT")
    private String reason; // Broken, Expired, Theft, etc.

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User reportedBy;

    @Column(nullable = false)
    private LocalDateTime dateReported = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private DFLType type;

    public enum DFLType {
        BREAKAGE, EXPIRATION, THEFT, UNKNOWN
    }
}
