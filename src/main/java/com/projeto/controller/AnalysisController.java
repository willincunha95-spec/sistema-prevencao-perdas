package com.projeto.controller;

import com.projeto.model.Analysis;
import com.projeto.model.DFL;
import com.projeto.model.Found;
import com.projeto.service.AnalysisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyses")
@CrossOrigin("*")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping
    public ResponseEntity<Page<Analysis>> findAll(@RequestParam(required = false) Analysis.AnalysisStatus status,
            Pageable pageable) {
        Pageable safePageable = pageable != null ? pageable : Pageable.unpaged();
        return ResponseEntity.ok(analysisService.findAll(status, safePageable));
    }

    @PostMapping
    public ResponseEntity<Analysis> create(@RequestBody Analysis analysis) {
        if (analysis == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(analysisService.save(analysis));
    }

    @PostMapping("/{id}/to-dfl")
    public ResponseEntity<Void> convertToDFL(@PathVariable Long id, @RequestBody DFL dfl) {
        if (id == null || dfl == null)
            return ResponseEntity.badRequest().build();
        analysisService.markAsDFL(id, dfl);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/to-found")
    public ResponseEntity<Void> convertToFound(@PathVariable Long id, @RequestBody Found found) {
        if (id == null || found == null)
            return ResponseEntity.badRequest().build();
        analysisService.markAsFound(id, found);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Analysis>> findByResponsible(@PathVariable Long userId, Pageable pageable) {
        if (userId == null)
            return ResponseEntity.badRequest().build();
        Pageable safePageable = pageable != null ? pageable : Pageable.unpaged();
        return ResponseEntity.ok(analysisService.findByResponsible(userId, safePageable));
    }
}
