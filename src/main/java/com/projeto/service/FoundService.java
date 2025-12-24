package com.projeto.service;

import com.projeto.model.Found;
import com.projeto.repository.FoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoundService {

    private final FoundRepository foundRepository;

    @Autowired
    public FoundService(FoundRepository foundRepository) {
        this.foundRepository = foundRepository;
    }

    public Page<Found> findAll(Pageable pageable) {
        if (pageable == null)
            pageable = Pageable.unpaged();
        return foundRepository.findAll(pageable);
    }

    @Transactional
    public Found save(Found found) {
        if (found == null)
            throw new IllegalArgumentException("Found item cannot be null");
        return foundRepository.save(found);
    }
}
