package com.projeto.controller;

import com.projeto.model.ErrorLog;
import com.projeto.service.ErrorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/error-logs")
@CrossOrigin("*")
public class ErrorLogController {

    @Autowired
    private ErrorLogService errorLogService;

    @GetMapping
    public ResponseEntity<Page<ErrorLog>> findAll(Pageable pageable) {
        Pageable safePageable = pageable != null ? pageable : Pageable.unpaged();
        return ResponseEntity.ok(errorLogService.findAll(safePageable));
    }

    @PostMapping
    public ResponseEntity<ErrorLog> create(@RequestBody ErrorLog errorLog) {
        if (errorLog == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(errorLogService.save(errorLog));
    }
}
