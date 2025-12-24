package com.projeto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "found_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Found {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String locationFound;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String color;

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

    @ManyToOne
    @JoinColumn(name = "found_by_user_id", nullable = false)
    private User foundBy;

    @Column
    private java.math.BigDecimal salePrice;

    @Column
    private java.math.BigDecimal compensationPrice;

    @Column(nullable = false)
    private LocalDateTime dateFound = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private FoundStatus status;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = FoundStatus.STORED;
        }
    }

    public enum FoundStatus {
        STORED, CLAIMED, DISCARDED
    }
}
