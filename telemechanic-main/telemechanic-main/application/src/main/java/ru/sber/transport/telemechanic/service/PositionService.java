package ru.sber.transport.telemechanic.service;


import ru.sber.transport.telemechanic.database.model.Position;

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
    
    /**
     * Сохраняем должность, которую мы получим по grpc из сервиса corporate
     *
     * @param message Сообщение в случае ошибки
     * @param id Идентификатор записи о должности
     */
    void saveGrpcEntity(String message, UUID id);
}
