package ru.sberbank.ditsib.transport.tariff.service;

import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    GeoZone save(GeoZone geoZone);
    
    /**
     * Поиск геозоны по имени.
     *
     * @param geoZone имя геозоны.
     * @return геозона.
     */
    Optional<GeoZone> find(String geoZone);
    
    /**
     * Получение геозон по идентификаторам.
     *
     * @param regionIds идентификаторы.
     * @return список геозон.
     */
    List<GeoZone> get(Set<UUID> regionIds);
    
    /**
     * Поиск региона.
     *
     * @param region регион.
     * @return геозона.
     */
    Optional<GeoZone> search(String region);
}
