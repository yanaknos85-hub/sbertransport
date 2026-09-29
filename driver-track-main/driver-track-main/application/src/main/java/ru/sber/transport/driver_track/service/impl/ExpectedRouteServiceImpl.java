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
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedWaypointsTripRecord;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.repository.ExpectedRouteRepository;
import ru.sber.transport.driver_track.service.CalculateRouteService;
import ru.sber.transport.driver_track.service.ExpectedRouteService;
import ru.sber.transport.driver_track.service.ExpectedWaypointsTripService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExpectedRouteServiceImpl implements ExpectedRouteService {

    private final ObjectMapper objectMapper;
    private final ExpectedRouteRepository expectedRouteRepository;
    private final ExpectedWaypointsTripService expectedWaypointsTripService;
    private final CalculateRouteService calculateRouteService;

    @Override
    @SneakyThrows(JsonProcessingException.class)
    public RouteDTO getExpectedRoute(UUID tripId, RouteSource sourceType) {
        var routeRecordOpt = expectedRouteRepository.findByTripIdAndSource(tripId, sourceType);
        if (routeRecordOpt.isEmpty()) {
            return null;
        }

        var routeRecord = routeRecordOpt.get();
        return objectMapper.readValue(routeRecord.getCoords().data(), RouteDTO.class);
    }

    @Transactional
    @Override
    public void createExpectedRoute() {
        var expectedWaypointsTrips = expectedWaypointsTripService.getAllNotHandledRecords();
        if (expectedWaypointsTrips.isEmpty()) { return; }

        var expectedRouteRecordList = expectedWaypointsTrips.stream()
                .flatMap(trip ->(calculateRoute(trip)).stream())
                .toList();

        var idTripsToRemove = expectedRouteRecordList.stream()
                .map(ExpectedRouteRecord::getTripId)
                .collect(Collectors.toSet());

        var expectedRouteWithWrongWaypointsUUIDList = expectedWaypointsTrips.stream()
                .filter(item -> !idTripsToRemove.contains(item.getId()))
                .map(ExpectedWaypointsTripRecord::getTripId)
                .toList();

        expectedWaypointsTripService.setMaxAttemptsCountToWrongRouteGeneration(expectedRouteWithWrongWaypointsUUIDList);


        expectedRouteRecordList.forEach(expectedRouteRepository::insertIfNotExist);

        expectedWaypointsTripService.handleCompliteCreateExpectedRoute(expectedRouteRecordList);
    }

    private List<ExpectedRouteRecord> calculateRoute(ExpectedWaypointsTripRecord trip) {
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
        return routes
                .entrySet()
                .stream()
                .map(kvp -> createExpectedRouteRecord(kvp.getValue(), tripId, kvp.getKey()))
                .toList();
    }

    @SneakyThrows({JsonProcessingException.class})
    private ExpectedRouteRecord createExpectedRouteRecord(RouteDTO routeDTO, UUID tripId, RouteSource routeSource){
        var routeJson = JSON.valueOf(objectMapper.writeValueAsString(routeDTO));
        return new ExpectedRouteRecord(UUID.randomUUID(), tripId, routeJson, routeSource.name());
    }



}
