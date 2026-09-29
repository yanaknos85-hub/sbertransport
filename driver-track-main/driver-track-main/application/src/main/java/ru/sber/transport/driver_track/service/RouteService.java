package ru.sber.transport.driver_track.service;

import ru.sber.transport.driver_track.database.driver_track.tables.records.FactWaypointsTripRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с координатами.
 */
public interface RouteService {



    /**
     * Вычисление фактических маршрутов поездки.
     *
     * @param trip идентификатор поездки.
     */
    List<RouteRecord> calculateRoute(FactWaypointsTripRecord trip);

    /**
     * Получение фактического маршрута.
     *
     * @param tripId     идентификатор поездки.
     * @param sourceType источник данных.
     * @return фактический маршрут.
     */
    RouteDTO getRoute(UUID tripId, RouteSource sourceType);

    /**
     * Создание записи с фактическими координатами маршрута.
     * @param tripId
     */
    void createFactWaypointForTrip(UUID tripId);

    /**
     * Формирование фактических маршрутов по формуле и из 2_ГИС
     */
    void createFactRoute();
}
