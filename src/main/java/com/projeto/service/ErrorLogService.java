package com.projeto.service;

import com.projeto.model.ErrorLog;
import com.projeto.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    @Autowired
    public ErrorLogService(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    public Page<ErrorLog> findAll(Pageable pageable) {
        if (pageable == null)
            pageable = Pageable.unpaged();
        return errorLogRepository.findAll(pageable);
    }

    public ErrorLog save(ErrorLog errorLog) {
        if (errorLog == null)
            throw new IllegalArgumentException("ErrorLog cannot be null");
        if (errorLog.getSeverity() == null) {
            errorLog.setSeverity(ErrorLog.ErrorSeverity.MEDIUM);
        }
        return errorLogRepository.save(errorLog);
    }
}
