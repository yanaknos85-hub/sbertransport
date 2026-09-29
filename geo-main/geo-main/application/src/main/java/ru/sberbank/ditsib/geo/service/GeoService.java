package ru.sberbank.ditsib.geo.service;

import ru.sberbank.ditsib.geo.config.properties.routing.DistanceUnit;
import ru.sberbank.ditsib.geo.config.properties.routing.RouteType;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.WaypointDto;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.model.Route;

import java.util.Collection;
import java.util.List;

/**
 * Interface for working with GEO-service.
 */
public interface GeoService {

    /**
     * Get address by coordinates.
     *
     * @param addressRequest params of address request.
     * @return data from service.
     */
    Collection<Address> getAddressesByCoordinates(AddressRequestDto addressRequest);

    /**
     * Get addresses by location string.
     *
     * @param addressRequest params of address request.
     * @return coordinates.
     */
    Collection<Address> getAddressByLocation(AddressRequestDto addressRequest);

    /**
     * Get route by coordinates.
     *
     * @param coordinates          coordinates to get route.
     * @param distanceUnit         distance unit.
     * @param routeType            type of route.
     * @param transportServiceType type of transport service
     * @return route.
     */
    List<Route> getRoutes(
            List<WaypointDto> coordinates, DistanceUnit distanceUnit, RouteType routeType, String transportServiceType
    );
}
