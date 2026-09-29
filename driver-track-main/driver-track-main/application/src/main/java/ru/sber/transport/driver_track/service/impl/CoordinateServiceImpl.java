package ru.sber.transport.driver_track.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.dto.BatchCoordinateRequest;
import ru.sber.transport.driver_track.dto.BatchPointDTO;
import ru.sber.transport.driver_track.mapper.CoordinateMapper;
import ru.sber.transport.driver_track.dto.GeoWaypointDTO;
import ru.sber.transport.driver_track.repository.CoordinateRepository;
import ru.sber.transport.driver_track.repository.DriverRepository;
import ru.sber.transport.driver_track.service.CoordinateService;
import ru.sber.transport.trip.message.TripMessage;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinateServiceImpl implements CoordinateService {
    private final DriverRepository driverRepository;
    private final CoordinateRepository coordinateRepository;
    private final CoordinateMapper coordinateMapper;

    @Override
    public void savePointInfo(GeoWaypointDTO geoWaypointDTO, UUID driverId) {
        var driver = driverRepository.getByIdNotNull(driverId);
        if (!driver.getOnline()) {
            return;
        }

        var tripId = driver.getActiveTripId();
        if (tripId == null) {
            return;
        }

        var coordinate = new CoordinateRecord(UUID.randomUUID(), tripId, geoWaypointDTO.getLatitude(),
                geoWaypointDTO.getLongitude(), LocalDateTime.now(ZoneOffset.UTC));
        coordinateRepository.save(coordinate);
    }

    @Override
    public void savePointInfo(TripMessage message) {
        if (message.getWaypoints() == null || message.getWaypoints().isEmpty())
            return;

        List<CoordinateRecord> coordinateRecordList = message.getWaypoints().stream()
                .filter(waypoint -> waypoint.get("latitude") != null && waypoint.get("longitude") != null)
                .map(waypoint ->  new CoordinateRecord(
                        UUID.randomUUID(),
                        message.getId(),
                        (Double) waypoint.get("latitude"),
                        (Double) waypoint.get("longitude"),
                        LocalDateTime.now(ZoneOffset.UTC))
                ).toList();
        if (!coordinateRecordList.isEmpty())
            coordinateRepository.saveAll(coordinateRecordList);
    }

    @Override
    public void saveBatchPoints(BatchCoordinateRequest request, UUID driverId) {
        driverRepository.getByIdNotNull(driverId);

        var coordinateRecords = request.points().stream()
                .filter(this::isValidCoordinate)
                .map(point -> coordinateMapper.toCoordinateRecord(point, request.tripId()))
                .toList();

        if (!coordinateRecords.isEmpty())
            coordinateRepository.saveAll(coordinateRecords);
    }

    private boolean isValidCoordinate(BatchPointDTO point) {
        return point.latitude() != null
                && point.longitude() != null
                && point.timestamp() != null
                && point.latitude() >= -90 && point.latitude() <= 90
                && point.longitude() >= -180 && point.longitude() <= 180;
    }
}
