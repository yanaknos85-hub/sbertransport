package ru.sber.transport.driver_track.service;

import jakarta.validation.constraints.NotNull;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для расчета маршрута
 */
public interface CalculateRouteService {
    /**
     * Расчет маршрута
     * @param coords
     * @param tripId
     * @return Возвращает маршруты построенные на основе координат и 2GIS
     */
    Map<RouteSource, RouteDTO> calculateRoute(List<CoordinateRecord> coords, UUID tripId);

    /**
     * Подготавливает координаты для построения маршрута
     * @param coords
     * @return Возвращает координаты, которые будут использоваться для построения маршрута
     */
    List<CoordinateRecord> prepareCoords(List<CoordinateRecord> coords);
}
