package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.transport.oto.cargo.database.model.Position;

import java.util.Optional;
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
    
    Position findOrCreatePositionById(UUID id);
}
