package com.projeto.repository;

import com.projeto.model.Register;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisterRepository extends JpaRepository<Register, Long> {
    Page<Register> findByProtocol(String protocol, Pageable pageable);

    Page<Register> findByStatus(Register.RegisterStatus status, Pageable pageable);

    long countByStatus(Register.RegisterStatus status);
}
