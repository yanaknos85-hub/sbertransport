package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.messages.GeoZone;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с геозонами.
 */
public interface GeoZoneService {
    
    /**
     * Получение гео-зоны.
     *
     * @param id идентификатор.
     * @return геозона.
     */
    Optional<GeoZone> get(UUID id);
    
    /**
     * Удаление геозоны.
     *
     * @param geoZone геозона для удаления.
     */
    void delete(GeoZone geoZone);
    
    /**
     * Сохранение геозоны.
     *
     * @param geoZone геозона для сохранения.
     */
    void save(GeoZone geoZone);
    
}
