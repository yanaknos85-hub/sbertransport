package ru.sberbank.ditsib.geo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.geo.dto.*;
import ru.sberbank.ditsib.geo.mappers.AddressMapper;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.model.Route;
import ru.sberbank.ditsib.geo.service.GeoControllerService;
import ru.sberbank.ditsib.geo.service.GeoService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of geo service.
 */
@SuppressWarnings("java:S3958")
@RequiredArgsConstructor
@Component
@Slf4j
class GeoControllerServiceImpl implements GeoControllerService {
    
    private final GeoService geoService;
    
    private final AddressMapper addressMapper;
    
    @Override
    public Collection<AddressDto> getAddress(
            AddressRequestDto addressRequest
                                                    ) {
        var result = new ArrayList<Address>();
        if (addressRequest.getLatitude() != null && addressRequest.getLongitude() != null) {
            result.addAll(geoService.getAddressesByCoordinates(addressRequest));
        }
        if (StringUtils.hasText(addressRequest.getLocation())) {
            result.addAll(geoService.getAddressByLocation(addressRequest));
        }
        return result.stream().map(addressMapper::toDto).toList();
    }
    
    @Override
    public List<RouteDto> getRoutes(RouteRequestDto routeRequest) {
        var distanceUnit = routeRequest.getDistanceUnit();
        var routeType = routeRequest.getRouteType();
        var transportService = routeRequest.getTransportServiceType();
        var collect = geoService.getRoutes(routeRequest.getCoordinates(), distanceUnit, routeType, transportService).stream()
                                .map(this::convertToResponse)
                                .toList();
        log.info(String.format("Found %d routes", collect.size()));
        return collect;
    }
    
    /**
     * Convert data to response.
     *
     * @param route route.
     *
     * @return response.
     */
    private RouteDto convertToResponse(Route route) {
        var pointDtos = new ArrayList<RoutePartDto>();
    
        for (var point : route.getSegments()) {
            var maneuverDtos = point.getCoordinates()
                                    .stream().map(maneuver ->
                                                          CoordinatesDto.builder()
                                                                        .longitude(maneuver.getLongitude())
                                                                        .latitude(maneuver.getLatitude())
                                                                        .build())
                                    .toList();
        
            pointDtos.add(RoutePartDto.builder()
                                      .distance(point.getDistance())
                                      .time(point.getTime())
                                      .coordinates(maneuverDtos).build());
        }
    
        var waypoints = route.getWaypoints()
                             .stream()
                             .map(waypoint -> new WaypointDto(AddressDto.builder()
                                                                        .city(waypoint.getCity())
                                                                        .country(waypoint.getCountry())
                                                                        .house(waypoint.getHouse())
                                                                        .latitude(waypoint.getId().getLatitude())
                                                                        .longitude(waypoint.getId().getLongitude())
                                                                        .region(waypoint.getRegion())
                                                                        .district(waypoint.getDistrict())
                                                                        .street(waypoint.getStreet())
                                                                        .build(),
                                                              waypoint.getWaitTime()))
                             .toList();
    
        return RouteDto.builder()
                       .distance(route.getDistance())
                       .time(route.getTime())
                       .segments(pointDtos)
                       .waypoints(waypoints)
                       .build();
    
    }
    
}
