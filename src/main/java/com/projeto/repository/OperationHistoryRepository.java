package com.projeto.repository;

import com.projeto.model.OperationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationHistoryRepository extends JpaRepository<OperationHistory, Long> {
    Page<OperationHistory> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);
}
