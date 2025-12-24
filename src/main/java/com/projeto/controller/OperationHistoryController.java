package com.projeto.controller;

import com.projeto.model.OperationHistory;
import com.projeto.service.OperationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
@CrossOrigin("*")
public class OperationHistoryController {

    @Autowired
    private OperationHistoryService historyService;

    @GetMapping
    public ResponseEntity<Page<OperationHistory>> findAll(Pageable pageable) {
        return ResponseEntity.ok(historyService.findAll(pageable));
    }
}
