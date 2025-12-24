package com.projeto.service;

import com.projeto.model.DFL;
import com.projeto.repository.DFLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DFLService {

    private final DFLRepository dflRepository;

    @Autowired
    public DFLService(DFLRepository dflRepository) {
        this.dflRepository = dflRepository;
    }

    public Page<DFL> findAll(Pageable pageable) {
        if (pageable == null)
            pageable = Pageable.unpaged();
        return dflRepository.findAll(pageable);
    }

    @Transactional
    public DFL save(DFL dfl) {
        if (dfl == null)
            throw new IllegalArgumentException("DFL cannot be null");
        // Validate quantities, etc.
        if (dfl.getQuantity() != null && dfl.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        return dflRepository.save(dfl);
    }
}
