package ru.sber.transport.notifications.services;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.model.limits.Limit;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по работе с лимитами.
 */
public interface LimitService {
    
    /**
     * Получение лимита по идентификатору.
     *
     * @param id идентификатор лимита.
     * @return лимит.
     */
    Optional<Limit> get(UUID id);
    
    /**
     * Получение лимита по идентификатору.
     *
     * @param id идентификатор лимита.
     * @param type вид транспорта.
     * @return лимит.
     */
    Optional<Limit> get(UUID id, TransportTypeEnum type);
    
    /**
     * Сохранение лимитов.
     *
     * @param limit объект для сохранения.
     */
    void save(Limit limit);
}
