package ru.sber.transport.notifications.database.dao.messages.approve;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.approve.TripApprove;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с согласованиями.
 */
public interface TripApproveRepository extends JpaRepository<TripApprove, UUID> {
    
    /**
     * Поиск согласования по идентификатору заявки.
     *
     * @param requestId идентификатор заявки.
     * @return согласование.
     */
    Optional<TripApprove> findByRequestId(UUID requestId);
    
}
