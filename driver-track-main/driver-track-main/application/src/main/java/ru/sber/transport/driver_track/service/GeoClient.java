package ru.sber.transport.driver_track.service;

import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.dto.RouteDTO;

import java.util.List;

/**
 * Клиент гео-провайдера.
 */
public interface GeoClient {

    /**
     * Воссоздание маршрута по фактическим координатам.
     *
     * @param coords координаты.
     * @return восстановленный маршрут.
     */
    RouteDTO recreateRoute(List<CoordinateRecord> coords);
}
