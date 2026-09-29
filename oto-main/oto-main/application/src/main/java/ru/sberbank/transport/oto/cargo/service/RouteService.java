package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.transport.oto.cargo.database.model.Routelist;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по операциям с маршрутом
 */
public interface RouteService {
    
    /**
     * Поиск маршрута по ID
     *
     * @param id ID маршрута
     * @return маршрутный лист
     */
    Optional<Routelist> getOptional(UUID id);
    
    /**
     * Сохранение маршрута
     * @param routelist маршрутный лист
     * @return сохраненный маршрутный лист
     */
    Routelist save(Routelist routelist);
}
