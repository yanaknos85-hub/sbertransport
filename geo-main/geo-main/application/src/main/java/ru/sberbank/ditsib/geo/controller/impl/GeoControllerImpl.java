package ru.sberbank.ditsib.geo.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.geo.controller.GeoController;
import ru.sberbank.ditsib.geo.dto.*;
import ru.sberbank.ditsib.geo.service.GeoControllerService;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation of GEO-controller.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Scope("request")
class GeoControllerImpl implements GeoController {
    
    private final GeoControllerService service;
    
    @Override
    public Collection<AddressDto> getAddress(
            AddressRequestDto requestDto
                                                    ) {
    
        log.debug("""
                  Request for searching address with data:
                    string=%s
                    latitude=%s
                    longitude=%s""".formatted(requestDto.getLocation(),
                requestDto.getLatitude(), requestDto.getLongitude()));
    
        return service.getAddress(requestDto);
    }
    
    @Override
    public RouteDto getRoute(
            RouteRequestDto routeRequest
                            ) {
        try {
            return getRoutes(routeRequest).getFirst();
        } catch (NoSuchElementException e) {
            log.warn("Не удалось построить маршрут", e);
        }

        return RouteDto.empty();
    }
    
    @Override
    public List<RouteDto> getRoutes(
            RouteRequestDto routeRequest
                                   ) {
        var routes = service.getRoutes(routeRequest);
        log.info(String.format("Found %d routes", routes.size()));
        return routes;
    }
    
    @Override
    public RegionDto getRegion(WaypointDto waypoint) {
        return RegionDto.builder().code(50).name("Москва").build();
    }
}
