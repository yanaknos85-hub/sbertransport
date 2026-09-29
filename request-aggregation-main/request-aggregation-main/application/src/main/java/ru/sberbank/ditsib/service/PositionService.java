package ru.sberbank.ditsib.service;


import ru.sberbank.ditsib.database.model.Position;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with positions.
 */
public interface PositionService {

    Optional<Position> get(UUID id);

    /**
     * Delete position.
     *
     * @param entity position to delete.
     */
    void delete(Position entity);

    /**
     * Save position.
     *
     * @param entity position to save.
     */
    Position save(Position entity);
}
