package ru.sber.transport.driver_track.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.driver_track.controller.RouteController;
import ru.sber.transport.driver_track.dto.BatchCoordinateRequest;
import ru.sber.transport.driver_track.dto.GeoWaypointDTO;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.dto.RouteType;
import ru.sber.transport.driver_track.service.CoordinateService;
import ru.sber.transport.driver_track.service.ExpectedRouteService;
import ru.sber.transport.driver_track.service.RouteService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Transactional
public class RouteControllerImpl implements RouteController {

    private final RouteService routeService;
    private final CoordinateService coordinateService;
    private final ExpectedRouteService expectedRouteService;

    @Override
    public void savePointInfo(GeoWaypointDTO geoWaypointDTO, JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        coordinateService.savePointInfo(geoWaypointDTO, userId);
    }

    @Override
    public ResponseEntity<RouteDTO> getRoute(UUID tripId, RouteSource sourceType, RouteType routeType) {
        RouteDTO resultRoute = null;
        if (routeType == RouteType.FACT) {
            resultRoute = routeService.getRoute(tripId, sourceType);
        } else {
            resultRoute = expectedRouteService.getExpectedRoute(tripId, sourceType);
        }

        return ResponseEntity.ok(resultRoute);
    }

    @Override
    public void saveBatchPoints(BatchCoordinateRequest request, JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        coordinateService.saveBatchPoints(request, userId);
    }
}
