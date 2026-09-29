package ru.sberbank.ditsib.geo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.geo.config.properties.routing.DistanceUnit;
import ru.sberbank.ditsib.geo.config.properties.routing.RouteType;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.WaypointDto;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.model.Route;
import ru.sberbank.ditsib.geo.service.AddressService;
import ru.sberbank.ditsib.geo.service.GeoService;
import ru.sberbank.ditsib.geo.service.RouteService;

import java.util.Collection;
import java.util.List;

/**
 * Реализация гео-сервиса.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class GeoServiceImpl implements GeoService {
    
    private final RouteService routeService;
    
    private final AddressService addressService;
    
    @Override
    public Collection<Address> getAddressesByCoordinates(AddressRequestDto addressRequest) {
        return addressService.getAddressByCoordinates(addressRequest);
    }
    
    @Override
    public Collection<Address> getAddressByLocation(AddressRequestDto addressRequest) {
        return addressService.getAddressByLocation(addressRequest);
    }
    
    @Override
    public List<Route> getRoutes(
            List<WaypointDto> coordinates, DistanceUnit distanceUnit, RouteType routeType, String transportService
                                ) {
        var routes = routeService.routeRequest(coordinates, distanceUnit, routeType, transportService);
        log.info(String.format("Find %s routes", routes.size()));
        return routes;
    }
    
}
