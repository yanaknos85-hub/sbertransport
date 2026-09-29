package ru.sber.transport.driver_track.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedWaypointsTripRecord;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.repository.ExpectedWaypointsTripRepository;
import ru.sber.transport.driver_track.service.ExpectedWaypointsTripService;
import ru.sber.transport.trip.message.TripMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExpectedWaypointsTripServiceImpl implements ExpectedWaypointsTripService {

    private final DSLContext dslContext;
    private final ObjectMapper objectMapper;
    private final ExpectedWaypointsTripRepository expectedWaypointsTripRepository;

    @Override
    public void save(TripMessage message) {
        if (message == null) {
            return;
        }

        log.info("Found new trip '%s' message".formatted(message.getId()));
        var expectedWaypoint = expectedWaypointsTripRepository.findByTripId(message.getId());
        if (expectedWaypoint.isPresent()) return;

        ExpectedWaypointsTripRecord expectedWaypointsTripRecord = dslContext.newRecord(expectedWaypointsTripRepository.table());
        expectedWaypointsTripRecord.setId(UUID.randomUUID());
        expectedWaypointsTripRecord.setTripId(message.getId());
        expectedWaypointsTripRecord.setWaypoints(toJson(message.getWaypoints()));
        expectedWaypointsTripRecord.setIsTrackCreated(false);
        expectedWaypointsTripRepository.save(expectedWaypointsTripRecord);
    }

    @Override
    public List<ExpectedWaypointsTripRecord> getAllNotHandledRecords() {
        return expectedWaypointsTripRepository.getAllNotHandledRecords();
    }

    @Override
    public void handleCompliteCreateExpectedRoute(@NotEmpty List<ExpectedRouteRecord> handledTripRecords) {
        var grouped = handledTripRecords.stream()
                .collect(Collectors.toMap(
                        ExpectedRouteRecord::getTripId,
                        item -> item,
                        (oldItem, newItem) -> RouteSource.TWO_GIS.name().equals(newItem.getSource()) ? newItem : oldItem
                ));

        expectedWaypointsTripRepository.handleCompliteCreateExpectedRoute(new ArrayList<>(grouped.values()));
    }

    @Override
    public void setMaxAttemptsCountToWrongRouteGeneration(List<UUID> expectedRouteWithWrongWaypointsUUIDList) {
        expectedWaypointsTripRepository.setMaxAttemptsCountToWrongRouteGeneration(expectedRouteWithWrongWaypointsUUIDList);
    }

    private JSON toJson(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return JSON.valueOf(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error serialization in JSON", e);
        }

    }
}
