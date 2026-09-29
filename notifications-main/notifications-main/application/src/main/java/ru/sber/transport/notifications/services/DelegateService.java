package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.coprorate.Delegate;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с делегаты.
 */
public interface DelegateService {
    
    /**
     * Получение делегата по id
     * @param id
     * @return сущность делегата
     */
    Optional<Delegate> get(UUID id);
    
    /**
     * Сохранение делегата
     * @param delegate сущность делегата
     * @return сущность делегата
     */
    Delegate save(Delegate delegate);
    
    /**
     * Удаление делегата по id
     * @param id
     */
    void delete(UUID id);
}
