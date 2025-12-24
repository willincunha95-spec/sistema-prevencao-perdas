package com.projeto.service;

import com.projeto.model.OperationHistory;
import com.projeto.repository.OperationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OperationHistoryService {

    private final OperationHistoryRepository historyRepository;

    @Autowired
    public OperationHistoryService(OperationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public Page<OperationHistory> findAll(Pageable pageable) {
        if (pageable == null)
            pageable = Pageable.unpaged();
        return historyRepository.findAll(pageable);
    }

    @Async // Run asynchronously to not block main operation
    public void logOperation(String operationType, String entityName, Long entityId, Long userId, String details) {
        OperationHistory history = new OperationHistory();
        history.setOperationType(operationType);
        history.setEntityName(entityName);
        history.setEntityId(entityId);
        // history.setPerformedBy(userRepository.findById(userId).orElse(null)); //
        // Simplification: handle user lookup if complex
        // For simplicity assuming passed user object or just skipping strict relation
        // here for async speed without more deps,
        // but let's do it right later if needed. For now, manual setting if object
        // passed or just null.
        // Actually let's keep it simple: Controller creates the object fully populated
        // or Service does.
        history.setDetails(details);

        historyRepository.save(history);
    }

    public OperationHistory save(OperationHistory history) {
        if (history == null)
            throw new IllegalArgumentException("History cannot be null");
        return historyRepository.save(history);
    }
}
