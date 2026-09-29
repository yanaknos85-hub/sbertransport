package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.request.SharedRide;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для получения данных по совместным поездкам
 */
public interface SharedRequestService {
    
    /**
     * Получение данных совместной поездки по id
     * @param id id совместной поездки
     * @return данные совместной поездки
     */
    Optional<SharedRide> get(UUID id);
    
    /**
     * Сохранение данных совместной поездки
     * @param sharedRide данные совместной поездки
     * @return данные совместной поездки
     */
    SharedRide save(SharedRide sharedRide);
}
