package ru.sberbank.ditsib.transport.request.service.corp;

import ru.sberbank.ditsib.transport.request.database.model.corp.Position;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PositionService {
    
    
    /**
     * Get position by ID.
     *
     * @param id ID of position.
     *
     * @return position.
     */
    Optional<Position> get(UUID id);
    
    /**
     * Delete position.
     *
     * @param position position.
     */
    void delete(Position position);
    
    /**
     * Save position.
     *
     * @param position position to save.
     */
    Position save(Position position);
    
    /**
     * Get positions by ids
     * @param positionIds collection of position ids
     * @return positions
     */
    List<Position> getByIds(Set<UUID> positionIds);
}
