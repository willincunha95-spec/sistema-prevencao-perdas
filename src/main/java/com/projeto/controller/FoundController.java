package com.projeto.controller;

import com.projeto.model.Found;
import com.projeto.service.FoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/found-items")
@CrossOrigin("*")
public class FoundController {

    @Autowired
    private FoundService foundService;

    @GetMapping
    public ResponseEntity<Page<Found>> findAll(Pageable pageable) {
        Pageable safePageable = pageable != null ? pageable : Pageable.unpaged();
        return ResponseEntity.ok(foundService.findAll(safePageable));
    }

    @PostMapping
    public ResponseEntity<Found> create(@RequestBody Found found) {
        if (found == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(foundService.save(found));
    }
}
