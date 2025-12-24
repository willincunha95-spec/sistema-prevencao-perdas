package com.projeto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

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

    @Column
    private java.math.BigDecimal salePrice;

    @Column
    private java.math.BigDecimal compensationPrice;

    @Column
    private String location;

    @Column
    private String week;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User responsible;

    @Column(nullable = false)
    private LocalDateTime dateAnalyzed = LocalDateTime.now();

    @Column
    private Long registerId;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = AnalysisStatus.PENDING;
        }
    }

    public enum AnalysisStatus {
        PENDING, IN_PROGRESS, COMPLETED, ARCHIVED
    }
}
