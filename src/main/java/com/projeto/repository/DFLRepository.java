package com.projeto.repository;

import com.projeto.model.DFL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DFLRepository extends JpaRepository<DFL, Long> {
    Page<DFL> findByReportedBy_Id(Long userId, Pageable pageable);
}
