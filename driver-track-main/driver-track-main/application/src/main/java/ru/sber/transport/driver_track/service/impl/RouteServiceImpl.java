package ru.sber.transport.driver_track.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jooq.JSON;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.driver_track.database.driver_track.tables.records.*;
import ru.sber.transport.driver_track.dto.*;
import ru.sber.transport.driver_track.messaging.sender.TripFactDistanceSender;
import ru.sber.transport.driver_track.repository.*;
import ru.sber.transport.driver_track.service.CalculateRouteService;
import ru.sber.transport.driver_track.service.FactWaypointsTripService;
import ru.sber.transport.driver_track.service.RouteService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final CoordinateRepository coordinateRepository;
    private final TripFactDistanceSender tripFactDistanceSender;
    private final CalculateRouteService calculateRouteService;
    private final ObjectMapper objectMapper;
    private final RouteRepository routeRepository;
    private final FactWaypointsTripService factWaypointsTripService;


    @Override
    public List<RouteRecord> calculateRoute(FactWaypointsTripRecord trip) {
        var tripId = trip.getTripId();
        List<CoordinateRecord> coords = List.of();
        if (trip.getWaypoints() != null) {
            try {
                List<HashMap<String, Object>> list = objectMapper.readValue(trip.getWaypoints().data(), new TypeReference<List<HashMap<String, Object>>>() {});
                coords = list == null ? List.of() :list.stream().map(item -> new CoordinateRecord(
                        UUID.randomUUID(),
                        tripId,
                        (Double) item.get("latitude"),
                        (Double) item.get("longitude"),
                        LocalDateTime.now()
                )).toList();
            } catch (IOException e) {
                log.error("Error to parse json waypoints ", e);
            }
        }
        coords = calculateRouteService.prepareCoords(coords);

        var routes = calculateRouteService.calculateRoute(coords, tripId);

        var routeRecords = routes
                .entrySet()
                .stream()
                .map(kvp -> createRouteRecord(kvp.getValue(), tripId, kvp.getKey()))
                .toList();
        tripFactDistanceSender.send(routes, tripId);

        return routeRecords;
    }
    @Transactional
    @Override
    public void createFactRoute() {
        var factWaypointsTrips = factWaypointsTripService.getAllNotHandledRecords();
        if (factWaypointsTrips.isEmpty()) { return; }

        var factRouteRecordList = factWaypointsTrips.stream()
                .flatMap(waypointTrip ->(calculateRoute(waypointTrip)).stream())
                .toList();
        var idTripsToRemove = factWaypointsTrips.stream()
                .map(FactWaypointsTripRecord::getTripId)
                .collect(Collectors.toSet());

        var factRouteWithWrongWaypointsUUIDList = factWaypointsTrips.stream()
                .filter(item -> !idTripsToRemove.contains(item.getId()))
                .map(FactWaypointsTripRecord::getTripId)
                .toList();

        factWaypointsTripService.setMaxAttemptsCountToWrongRouteGeneration(factRouteWithWrongWaypointsUUIDList);

        factRouteRecordList.forEach(routeRepository::insertIfNotExist);

        factWaypointsTripService.handleCompliteCreateExpectedRoute(factRouteRecordList);
    }

    @Transactional
    @Override
    public void createFactWaypointForTrip(UUID tripId) {
        var coords = coordinateRepository.findCoordinatesByTripId(tripId);

        if (!coords.isEmpty()) {

            List<Map<String, Double>> points = new ArrayList<>();
            coords.forEach(coord -> {
                Map<String, Double> point = new HashMap<>();
                point.put("latitude", coord.getLatitude());
                point.put("longitude", coord.getLongitude());
                points.add(point);
            });
            try {
                var json = JSON.valueOf(objectMapper.writeValueAsString(points));
                var factWaypointsTrip = new FactWaypointsTripRecord();
                factWaypointsTrip.setId(UUID.randomUUID());
                factWaypointsTrip.setTripId(tripId);
                factWaypointsTrip.setWaypoints(json);
                factWaypointsTrip.setIsTrackCreated(false);
                factWaypointsTripService.saveFactWaypointsTrip(factWaypointsTrip);
                coordinateRepository.deleteAllByTripId(tripId);
            } catch (JsonProcessingException e) {
                log.error("Error to parse json waypoints ", e);
            }
        }
    }

    @Override
    @SneakyThrows(JsonProcessingException.class)
    public RouteDTO getRoute(UUID tripId, RouteSource sourceType) {
        var routeRecordOpt = routeRepository.findByTripIdAndSource(tripId, sourceType);
        if (routeRecordOpt.isEmpty()) {
            return null;
        }

        var routeRecord = routeRecordOpt.get();
        return objectMapper.readValue(routeRecord.getCoords().data(), RouteDTO.class);
    }

    @SneakyThrows({JsonProcessingException.class})
    private RouteRecord createRouteRecord(RouteDTO routeDTO, UUID tripId, RouteSource routeSource){
        var routeJson = JSON.valueOf(objectMapper.writeValueAsString(routeDTO));
        return new RouteRecord(UUID.randomUUID(), tripId, routeJson, routeSource.name());
    }
}
