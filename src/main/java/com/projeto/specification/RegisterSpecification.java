package com.projeto.specification;

import com.projeto.model.Register;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RegisterSpecification {

    @NonNull
    public static Specification<Register> withFilters(String search, String floor, String sector, String street,
            String risk, String valueRange, Register.RegisterStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Status Filter\
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Global Search (ProductName or SKU)
            if (search != null && !search.isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("productName")), likePattern);
                Predicate skuLike = cb.like(cb.lower(root.get("sku")), likePattern);
                predicates.add(cb.or(nameLike, skuLike));
            }

            // Floor Filter
            if (floor != null && !floor.isEmpty()) {
                String floorVal = floor.replace("andar_", ""); // "andar_0" -> "0"
                if (!floorVal.isEmpty()) {
                    predicates.add(cb.equal(root.get("floor"), floorVal));
                }
            }

            // Street Filter
            if (street != null && !street.isEmpty()) {
                predicates.add(cb.equal(root.get("street"), street));
            }

            // Sector Filter (Location Prefixes!)
            if (sector != null && !sector.isEmpty()) {
                String locField = "location";
                if ("dv".equalsIgnoreCase(sector)) {
                    predicates.add(cb.like(cb.lower(root.get(locField)), "dv%"));
                } else if ("rk".equalsIgnoreCase(sector)) {
                    predicates.add(cb.like(cb.lower(root.get(locField)), "rk%"));
                } else if ("hv".equalsIgnoreCase(sector)) {
                    predicates.add(cb.like(cb.lower(root.get(locField)), "hv%"));
                } else if ("mtu".equalsIgnoreCase(sector)) {
                    predicates.add(cb.like(cb.lower(root.get(locField)), "mtu%"));
                } else if ("deposito".equalsIgnoreCase(sector)) {
                    predicates.add(cb.like(cb.lower(root.get(locField)), "%depósito%"));
                } else if ("docas".equalsIgnoreCase(sector)) {
                    predicates.add(cb.like(cb.lower(root.get(locField)), "%docas%"));
                } else if ("recebimento".equalsIgnoreCase(sector)) {
                    predicates.add(cb.like(cb.lower(root.get(locField)), "%recebimento%"));
                }
            }

            // Value Range Filter
            if (valueRange != null && !valueRange.isEmpty()) {
                if ("low".equalsIgnoreCase(valueRange)) {
                    predicates.add(cb.lessThan(root.get("salePrice"), new BigDecimal("60.00")));
                } else if ("medium".equalsIgnoreCase(valueRange)) {
                    predicates
                            .add(cb.between(root.get("salePrice"), new BigDecimal("60.00"), new BigDecimal("100.00")));
                } else if ("high".equalsIgnoreCase(valueRange)) {
                    predicates.add(cb.greaterThan(root.get("salePrice"), new BigDecimal("100.00")));
                }
            }

            // Risk Filter (Date diff) - Approximate logic for SQL
            // Critical >= 80 days
            if (risk != null && !risk.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                if ("critical".equalsIgnoreCase(risk)) {
                    LocalDateTime date80 = now.minusDays(80);
                    predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), date80));
                } else if ("high".equalsIgnoreCase(risk)) { // 60-80
                    LocalDateTime date60 = now.minusDays(60);
                    LocalDateTime date80 = now.minusDays(80);
                    predicates.add(cb.between(root.get("timestamp"), date80, date60));
                } else if ("medium".equalsIgnoreCase(risk)) { // 30-60
                    LocalDateTime date30 = now.minusDays(30);
                    LocalDateTime date60 = now.minusDays(60);
                    predicates.add(cb.between(root.get("timestamp"), date60, date30));
                } else if ("low".equalsIgnoreCase(risk)) { // < 30
                    LocalDateTime date30 = now.minusDays(30);
                    predicates.add(cb.greaterThan(root.get("timestamp"), date30));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
