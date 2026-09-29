package ru.sber.transport.driver_track.service;

import ru.sber.transport.driver_track.dto.BatchCoordinateRequest;
import ru.sber.transport.driver_track.dto.GeoWaypointDTO;
import ru.sber.transport.trip.message.TripMessage;

import java.util.UUID;

public interface CoordinateService {
    /**
     * Сохранение очередной точки водителя.
     *
     * @param geoWaypointDTO данные точки.
     * @param driverId       идентификатор водителя.
     */
    void savePointInfo(GeoWaypointDTO geoWaypointDTO, UUID driverId);
    /**
     * Сохранение точки маршрута.
     *
     * @param message плановые данные маршрута.
     */
    void savePointInfo(TripMessage message);
    /**
     * Пакетное сохранение координат.
     *
     * @param request пакет координат.
     * @param driverId идентификатор водителя.
     */
    void saveBatchPoints(BatchCoordinateRequest request, UUID driverId);
}
