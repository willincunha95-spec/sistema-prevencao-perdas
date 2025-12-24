package com.projeto.controller;

import com.projeto.model.DFL;
import com.projeto.service.DFLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dfls")
@CrossOrigin("*")
public class DFLController {

    @Autowired
    private DFLService dflService;

    @GetMapping
    public ResponseEntity<Page<DFL>> findAll(Pageable pageable) {
        Pageable safePageable = pageable != null ? pageable : Pageable.unpaged();
        return ResponseEntity.ok(dflService.findAll(safePageable));
    }

    @PostMapping
    public ResponseEntity<DFL> create(@RequestBody DFL dfl) {
        if (dfl == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(dflService.save(dfl));
    }
}
