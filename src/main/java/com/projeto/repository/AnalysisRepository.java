package com.projeto.repository;

import com.projeto.model.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    Page<Analysis> findByResponsible_Id(Long userId, Pageable pageable);

    Page<Analysis> findByStatus(Analysis.AnalysisStatus status, Pageable pageable);

    java.util.Optional<Analysis> findByRegisterId(Long registerId);

    long countByStatus(Analysis.AnalysisStatus status);
}
