package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;

import java.util.List;
import java.util.UUID;

/**
 * Repository of positions
 */
public interface PositionRepository extends JpaRepository<Position, UUID> {
    /**
     * Получить список организаций по флагу активности
     *
     * @param isActive флаг активности
     *
     * @return список организаций
     */
    List<Position> findAllByActive(boolean isActive);
    
    List<Position> findAllByIdIn(List<UUID> positionIds);
}
