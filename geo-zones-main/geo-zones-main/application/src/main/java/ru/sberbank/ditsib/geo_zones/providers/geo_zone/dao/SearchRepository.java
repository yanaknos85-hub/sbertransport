package ru.sberbank.ditsib.geo_zones.providers.geo_zone.dao;

import ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone;

import java.util.Optional;

/**
 * Репозиторий для поиска.
 */
public interface SearchRepository {
    
    /**
     * Поиск геозоны по данным.
     *
     * @param region регион.
     * @param district район.
     * @param city город.
     * @param street улица.
     * @param house дом.
     * @return геозоны.
     */
    Optional<GeoZone> search(String region, String district, String city, String street, String house);
    
}
