package ru.sber.transport.driver_track.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;
import ru.sber.transport.driver_track.mapper.DriverMapper;
import ru.sber.transport.driver_track.repository.DriverRepository;
import ru.sber.transport.driver_track.service.RouteService;
import ru.sber.transport.driver_track.service.DriverService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final RouteService routeService;

    @Override
    public void save(DriverMessage message) {
        log.info("Found new driver '%s' message".formatted(message.getId()));
        var record = driverRepository.findById(message.getId()).orElse(createDriver(message));

        var needDistanceCalculation = record.getServing() != null && record.getServing() && !message.serving();
        var tripId = record.getActiveTripId();

        driverMapper.update(record, message);
        driverRepository.save(record);

        if (needDistanceCalculation) {
            routeService.createFactWaypointForTrip(tripId);
        }
    }

    private DriverMessageRecord createDriver(DriverMessage message) {
        var record = new DriverMessageRecord();
        record.setId(message.getId());
        return record;
    }
}
