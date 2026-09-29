package ru.sberbank.ditsib.geo.service;

import ru.sberbank.ditsib.geo.config.properties.routing.DistanceUnit;
import ru.sberbank.ditsib.geo.config.properties.routing.RouteType;
import ru.sberbank.ditsib.geo.dto.WaypointDto;
import ru.sberbank.ditsib.geo.model.Coordinates;
import ru.sberbank.ditsib.geo.model.Route;
import ru.sberbank.ditsib.geo.model.RouteRecreationCoordinates;

import java.util.List;

/**
 * Сервис маршрутизации.
 */
public interface RouteService {

    /**
     * Запрос на построение маршрута.
     *
     * @param coordinates          координаты маршрута.
     * @param distanceUnit         единицы измерения.
     * @param routeType            тип маршрута.
     * @param transportServiceType тип транспортного сервиса
     * @return список маршрутов.
     */
    List<Route> routeRequest(
            List<WaypointDto> coordinates, DistanceUnit distanceUnit, RouteType routeType, String transportServiceType
    );

    /**
     * Запрос на восстановление маршрута по множеству координат.
     *
     * @param coordinates координаты маршрута.
     * @return маршрут
     */
    Route routeRequest(List<RouteRecreationCoordinates> coordinates);
}
