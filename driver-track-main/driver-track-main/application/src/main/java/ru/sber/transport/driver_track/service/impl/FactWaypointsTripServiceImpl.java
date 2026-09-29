package ru.sber.transport.driver_track.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.driver_track.database.driver_track.tables.records.FactWaypointsTripRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.repository.FactWaypointsTripRepository;
import ru.sber.transport.driver_track.service.FactWaypointsTripService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FactWaypointsTripServiceImpl implements FactWaypointsTripService {

    private final FactWaypointsTripRepository factWaypointsTripRepository;

    @Override
    public List<FactWaypointsTripRecord> getAllNotHandledRecords() {
        return factWaypointsTripRepository.getAllNotHandledRecords();
    }

    @Override
    public void handleCompliteCreateExpectedRoute(List<RouteRecord> handledTripRecords) {
        var grouped = handledTripRecords.stream()
                .collect(Collectors.toMap(
                        RouteRecord::getTripId,
                        item -> item,
                        (oldItem, newItem) -> RouteSource.TWO_GIS.name().equals(newItem.getSource()) ? newItem : oldItem
                ));

        factWaypointsTripRepository.handleCompliteCreateExpectedRoute(new ArrayList<>(grouped.values()));
    }

    @Override
    public void saveFactWaypointsTrip(FactWaypointsTripRecord factWaypointsTrip) {
        factWaypointsTripRepository.save(factWaypointsTrip);
    }

    @Override
    public void setMaxAttemptsCountToWrongRouteGeneration(List<UUID> expectedRouteWithWrongWaypointsUUIDList) {
        factWaypointsTripRepository.setMaxAttemptsCountToWrongRouteGeneration(expectedRouteWithWrongWaypointsUUIDList);
    }
}
