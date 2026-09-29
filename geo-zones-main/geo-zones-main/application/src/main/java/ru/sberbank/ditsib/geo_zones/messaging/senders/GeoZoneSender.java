package ru.sberbank.ditsib.geo_zones.messaging.senders;

import ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone;

/**
 * Отправитель геозон.
 */
public interface GeoZoneSender {
    
    /**
     * Отправка геозоны.
     *
     * @param geoZone геозона.
     * @param deleted флаг удаления.
     */
    void send(GeoZone geoZone, boolean deleted);
    
}
