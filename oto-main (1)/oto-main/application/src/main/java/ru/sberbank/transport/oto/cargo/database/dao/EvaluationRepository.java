package ru.sberbank.transport.oto.cargo.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.transport.oto.cargo.database.model.Evaluation;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository of Evaluation
 */
@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {
    
    /**
     * Find evaluation by request id
     * @param requestId id of request
     * @return {@link Optional<Evaluation>}
     */
    Optional<Evaluation> findByRequestId(UUID requestId);
}
