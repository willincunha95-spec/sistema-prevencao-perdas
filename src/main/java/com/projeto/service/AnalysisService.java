package com.projeto.service;

import com.projeto.model.Analysis;
import com.projeto.model.DFL;
import com.projeto.model.Found;
import com.projeto.model.Register;
import com.projeto.repository.AnalysisRepository;
import com.projeto.repository.DFLRepository;
import com.projeto.repository.FoundRepository;
import com.projeto.repository.RegisterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final RegisterRepository registerRepository;
    private final DFLRepository dflRepository;
    private final FoundRepository foundRepository;

    @Autowired
    public AnalysisService(AnalysisRepository analysisRepository,
            RegisterRepository registerRepository,
            DFLRepository dflRepository,
            FoundRepository foundRepository) {
        this.analysisRepository = analysisRepository;
        this.registerRepository = registerRepository;
        this.dflRepository = dflRepository;
        this.foundRepository = foundRepository;
    }

    public Page<Analysis> findAll(Analysis.AnalysisStatus status, @NonNull Pageable pageable) {
        if (status != null) {
            return analysisRepository.findByStatus(status, pageable);
        }
        return analysisRepository.findAll(pageable);
    }

    @Transactional
    public Analysis save(@NonNull Analysis analysis) {
        Long registerId = analysis.getRegisterId();
        if (registerId != null) {
            // Check for duplicate analysis
            Optional<Analysis> existingAnalysis = analysisRepository.findByRegisterId(registerId);
            if (existingAnalysis.isPresent()) {
                throw new IllegalStateException("Item já está em análise");
            }

            // Update Register status
            Optional<Register> registerOpt = registerRepository.findById(registerId);
            if (registerOpt.isPresent()) {
                Register reg = registerOpt.get();
                reg.setStatus(Register.RegisterStatus.ANALYZED);
                registerRepository.save(reg);
            }
        }
        return analysisRepository.save(analysis);
    }

    @Transactional
    public void markAsDFL(@NonNull Long analysisId, @NonNull DFL dfl) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("Análise não encontrada"));

        analysis.setStatus(Analysis.AnalysisStatus.COMPLETED);
        analysisRepository.save(analysis);

        dflRepository.save(dfl);
    }

    @Transactional
    public void markAsFound(@NonNull Long analysisId, @NonNull Found found) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("Análise não encontrada"));

        analysis.setStatus(Analysis.AnalysisStatus.COMPLETED);
        analysisRepository.save(analysis);

        foundRepository.save(found);
    }

    public Page<Analysis> findByResponsible(@NonNull Long userId, @NonNull Pageable pageable) {
        return analysisRepository.findByResponsible_Id(userId, pageable);
    }
}
