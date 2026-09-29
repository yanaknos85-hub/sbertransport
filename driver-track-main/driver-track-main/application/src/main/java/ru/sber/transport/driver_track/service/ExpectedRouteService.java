package ru.sber.transport.driver_track.service;

import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import java.util.UUID;

public interface ExpectedRouteService {

    /**
     * Получение планового маршрута.
     *
     * @param tripId     идентификатор поездки.
     * @param sourceType источник данных.
     * @return фактический маршрут.
     */
    RouteDTO getExpectedRoute(UUID tripId, RouteSource sourceType);

    /**
     * Формирование запланированных маршрутов по формуле  2_ГИС
     */
    void createExpectedRoute();
}
