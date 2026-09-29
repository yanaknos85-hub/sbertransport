package ru.sberbank.ditsib.geo.client;

import ru.sberbank.ditsib.geo.config.properties.routing.DistanceUnit;
import ru.sberbank.ditsib.geo.config.properties.routing.RouteType;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.WaypointDto;
import ru.sberbank.ditsib.geo.model.RouteRecreationCoordinates;

import java.util.List;
import java.util.Map;

/**
 * Client of geo service.
 */
public interface GeoServiceClient {

    /**
     * Get address by coordinates.
     *
     * @param addressRequest params of address request.
     * @return address.
     */
    List<Map<String, Object>> getAddressesByCoordinates(AddressRequestDto addressRequest);

    /**
     * Get addresses by location string.
     *
     * @param addressRequest params of address request.
     * @return coordinates.
     */
    List<Map<String, Object>> getAddressesByLocation(AddressRequestDto addressRequest);

    /**
     * Get route by coordinates.
     *
     * @param coordinates          coordinates.
     * @param distanceUnit         unit for getting distance.
     * @param routeType            type of route
     * @param transportServiceType type of transport service
     * @param excludeDirtRoad      exclude dirt road
     * @return route.
     */
    List<Map<String, Object>> getRoutes(
        List<WaypointDto> coordinates,
        DistanceUnit distanceUnit, RouteType routeType, String transportServiceType,
        Boolean excludeDirtRoad
    );

    /**
     * Восстановление маршрута по точкам.
     *
     * @param coords координаты.
     * @return маршрут.
     */
    Map<String, Object> getRoute(List<RouteRecreationCoordinates> coords);

    /**
     * Get region by ID.
     *
     * @param id ID of region.
     * @return map with region data.
     */
    Map<String, Object> getRegion(String id);
}
