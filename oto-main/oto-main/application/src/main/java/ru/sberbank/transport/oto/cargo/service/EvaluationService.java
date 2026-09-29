package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.transport.oto.cargo.database.model.Evaluation;

import java.util.Optional;
import java.util.UUID;


public interface EvaluationService {
    
    /**
     * Get evaluation by request id
     * @param requestId id of request
     * @return {@link Optional<Evaluation>}
     */
    Optional<Evaluation> get(UUID requestId);
    
    /**
     * Save evaluation
     * @param evaluation {@link Evaluation}
     * @return {@link Evaluation}
     */
    Evaluation save(Evaluation evaluation);
}
