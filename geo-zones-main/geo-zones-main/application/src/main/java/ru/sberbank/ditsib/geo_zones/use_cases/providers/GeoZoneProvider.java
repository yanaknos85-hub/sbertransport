package ru.sberbank.ditsib.geo_zones.use_cases.providers;

import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZoneWithChildren;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер геозон.
 */
public interface GeoZoneProvider {
    
    /**
     * Получение геозоны.
     *
     * @param id идентифткатор.
     *
     * @return геозона.
     */
    Optional<GeoZone> get(UUID id);
    
    /**
     * Получение геозоны по коду.
     *
     * @param code код геозоны.
     *
     * @return геозона.
     */
    Optional<GeoZone> getByCodeFirstLevel(String code);
    
    Optional<GeoZone> getByCode(String code);
    
    /**
     * Получение геозоны по названию.
     *
     * @param name название геозоны.
     *
     * @return геозона.
     */
    Optional<GeoZone> getByName(String name);
    
    /**
     * Получение геозоны по коду.
     *
     * @param code код геозоны.
     * @param exclusion исключение из поиска.
     *
     * @return геозона.
     */
    Optional<GeoZone> getByCodeFirstLevel(String code, UUID exclusion);
    
    /**
     * Получение геозоны по названию.
     *
     * @param name название геозоны.
     * @param exclusion исключение из поиска.
     *
     * @return геозона.
     */
    Optional<GeoZone> getByName(String name, UUID exclusion);
    
    /**
     * Сохранение данных геозоны.
     *
     * @param geoZone геозона.
     *
     * @return данные для сохранения.
     */
    GeoZone save(GeoZone geoZone);
    
    /**
     * Удаление геозоны.
     *
     * @param geoZone геозона для удаления.
     */
    void delete(GeoZone geoZone);
    
    /**
     * Получение списка геозон.
     *
     * @return геозоны.
     */
    List<GeoZone> get();
    
    
    /**
     * Получение списка корневых геозон.
     *
     * @return геозоны.
     */
    List<GeoZoneWithChildren> getRoots();
    
    
    /**
     * Получение списка дочерних геозон.
     *
     * @return список геозон.
     */
    List<GeoZone> getChildren(UUID parentId);
    
    /**
     * Поиск геозоны по данным.
     *
     * @param region регион.
     * @param district район.
     * @param city город.
     * @param street улица.
     * @param house дом.
     *
     * @return геозоны.
     */
    GeoZone search(String region, String district, String city, String street, String house);
    
    /**
     * Поиск ветки геозон по данным.
     *
     * @param region регион.
     * @param district район
     * @param city город.
     * @param street улица.
     * @param house дом.
     *
     * @return геозоны.
     */
    List<GeoZone> searchBranch(String region, String district, String city, String street, String house);
}
