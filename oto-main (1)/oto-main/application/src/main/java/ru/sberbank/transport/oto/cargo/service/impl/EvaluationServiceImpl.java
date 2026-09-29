package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.transport.oto.cargo.database.dao.EvaluationRepository;
import ru.sberbank.transport.oto.cargo.database.model.Evaluation;
import ru.sberbank.transport.oto.cargo.service.EvaluationService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationServiceImpl implements EvaluationService {
    
    private final EvaluationRepository evaluationRepository;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Evaluation> get(UUID requestId) {
        return evaluationRepository.findByRequestId(requestId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Evaluation save(Evaluation evaluation) {
        var result = evaluationRepository.findByRequestId(evaluation.getRequest().getId());
        if (result.isEmpty()) {
            evaluationRepository.save(evaluation);
        } else {
            log.warn("Evaluation for Request with id = {}, already exist", result.get().getRequest().getId());
        }
        
        return evaluation;
    }
}
