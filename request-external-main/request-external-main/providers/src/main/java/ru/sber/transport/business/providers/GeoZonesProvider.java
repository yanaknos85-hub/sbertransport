package ru.sber.transport.business.providers;

import ru.sber.transport.request.external.model.geozone.GeoZoneDTO;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;

/**
 * Провайдер работы с геозонами
 */
public interface GeoZonesProvider {

    /**
     * Получает геозону по путевой точке
     *
     * @param waypoint путевая точка
     * @return геозона
     */
    GeoZoneDTO get(WaypointDTO waypoint);
}
