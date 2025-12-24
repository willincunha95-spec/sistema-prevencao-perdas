package com.projeto.controller;

import com.projeto.model.Analysis;
import com.projeto.model.Register;
import com.projeto.repository.AnalysisRepository;
import com.projeto.repository.DFLRepository;
import com.projeto.repository.FoundRepository;
import com.projeto.repository.RegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    @Autowired
    private RegisterRepository registerRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private DFLRepository dflRepository;

    @Autowired
    private FoundRepository foundRepository;

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();

        // Count PENDING registers (Pendentes)
        stats.put("registersPending", registerRepository.countByStatus(Register.RegisterStatus.PENDING));

        // Count PENDING analyses (Em Análise)
        stats.put("analysesInProgress", analysisRepository.countByStatus(Analysis.AnalysisStatus.PENDING));

        // Count Total DFLs
        stats.put("dflsTotal", dflRepository.count());

        // Count Total Founds
        stats.put("foundsTotal", foundRepository.count());

        // Placeholders for future modules
        stats.put("systemicTotal", 0L);
        stats.put("foundInvTotal", 0L);

        return stats;
    }
}
