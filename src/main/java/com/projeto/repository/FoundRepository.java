package com.projeto.repository;

import com.projeto.model.Found;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoundRepository extends JpaRepository<Found, Long> {
    Page<Found> findByStatus(Found.FoundStatus status, Pageable pageable);
}
