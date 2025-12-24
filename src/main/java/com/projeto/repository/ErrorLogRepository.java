package com.projeto.repository;

import com.projeto.model.ErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    Page<ErrorLog> findBySeverity(ErrorLog.ErrorSeverity severity, Pageable pageable);
}
