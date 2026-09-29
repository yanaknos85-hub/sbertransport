package ru.sber.transport.trips.cargo.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.trips.cargo.business.model.CargoRequest;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.ChannelType;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trips.cargo.web.service.DriverAssigningService;
import ru.sber.transport.trips.cargo.web.service.DriverService;
import ru.sber.transport.trips.cargo.web.service.UpdateTripService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class DriverAssigningServiceImpl implements DriverAssigningService {

    private static final String UNEXISTING_UUID = "00000000-0000-0000-0000-000000000000";

    private final DriverService driverService;

    private final TripProvider tripProvider;

    @Value("${driver.assign.finishSearchMin:10}")
    private int finishSearchMin;

    @Value("${driver.assign.triggerTimeSecs:1800}")
    private int triggerTimeSecs;

    @Value("${driver.assign.findtrip:00000000-0000-0000-0000-000000000000}")
    private String debugFindTripUUID;

    private final List<TripSender> tripSenders;

    private final UpdateTripService updateTripService;

    private final ContractorProvider contractorProvider;

    private final DriverSender driverSender;

    private final int DELTA = 5;

    @Override
    @Transactional
    public void assignDrivers() {
        try {
            var now = LocalDateTime.now(ZoneOffset.UTC);
            var tripsAll1 = tripProvider.findTripsForAssigningDriver();
            log.trace("assignDrivers: 1: found trips of size " + tripsAll1.size() + " listing: " + getList(tripsAll1));
            if (!UNEXISTING_UUID.equals(debugFindTripUUID)) {
                var found = tripsAll1.stream()
                        .filter(e -> e.getId().equals(UUID.fromString(debugFindTripUUID)))
                        .findFirst();
                if (found.isPresent()) {
                    long dur = Duration.between(now, found.get().getStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()).toSeconds();
                    log.trace("assignDrivers: DEBUGINFO: trip id " + debugFindTripUUID
                            + " found with start time = " + found.get().getStartTime()
                            + ", duration = " + dur);
                } else {
                    log.trace("assignDrivers: DEBUGINFO: trip id not found " + debugFindTripUUID);
                }
            }
            int stageSecs = 0;
            if (finishSearchMin < 0) {
                stageSecs = (finishSearchMin - DELTA) * 60;
            }
            final int finalStageSecs = stageSecs;
            log.trace("assignDrivers: finalStageSecs " + finalStageSecs);
            var tripsAll2 = tripsAll1.stream()
                    .filter(trip -> {
                        long dur = Duration.between(now, trip.getStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()).toSeconds();
                        return (dur > finalStageSecs && dur < triggerTimeSecs);
                    })
                    .toList();
            log.trace("assignDrivers: 2: found trips of size " + tripsAll2.size() + " listing: " + getList(tripsAll2));
            var tripsAll3 = tripsAll2.stream()
                    .filter(e -> !e.getRequests().isEmpty())
                    .filter(e -> contractorProvider.isAutoassign(e.getContractorId()))
                    .toList();
            for (Trip trip : tripsAll3) {
                if(trip.getCreationTime()==null) {
                    var optTripCreationTime = trip.getRequests().stream()
                            .map(CargoRequest::getCreationTime)
                            .filter(Objects::nonNull)
                            .min(OffsetDateTime::compareTo);
                    optTripCreationTime.ifPresent(trip::setCreationTime);
                }
            }
            var tripsAll4 = tripsAll3.stream()
                    .sorted(Comparator.comparing(Trip::getCreationTime))
                    .toList();
            log.trace("assignDrivers: 3: found trips of size " + tripsAll4.size() + " listing: " + getList(tripsAll4));
            var counter = 0;
            for (var trip : tripsAll4) {
                log.trace("assignDrivers: processing trip %s".formatted(trip.getId()));
                if (trip.getWaypoints().isEmpty()) {
                    log.warn("assignDriver: processing trip %s: no waypoints found!".formatted(trip.getId()));
                    continue;
                }
                if (LocalDateTime.now(ZoneOffset.UTC).isAfter(trip.getStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime().minusMinutes(finishSearchMin))) {
                    var updatedTrip = updateTripService.updateTrip(null, trip, TripStatus.ORDER_CANCELLED_BY_DRIVER);
                    tripSenders.forEach(sender -> sender.send(updatedTrip, false, ChannelType.KAFKA, ChannelType.WEB_SOCKET));
                    continue;
                }
                var driver = driverService.assignDriver(trip);
                if (driver != null) {
                    trip = updateTripService.updateTrip(driver, trip, TripStatus.DRIVER_ASSIGNED);
                    var finalTrip = trip;
                    tripSenders.forEach(sender -> sender.send(finalTrip, false, ChannelType.KAFKA, ChannelType.WEB_SOCKET));
                    driverSender.send(driver);
                    log.trace("assignDrivers: for trip %s driver assigned %s".formatted(trip.getId(), driver.getId()));
                    counter++;
                } else {
                    if (!UNEXISTING_UUID.equals(debugFindTripUUID) && trip.getId().equals(UUID.fromString(debugFindTripUUID))) {
                        log.trace("assignDrivers: driver for " + debugFindTripUUID + " not found");
                    }
                    log.trace("assignDrivers: for trip %s driver not found".formatted(trip.getId()));
                    updateTripService.incrementAutoAssignCounter(trip);
                }
            }
            log.info("assignDrivers: driver found for {} trips", counter);
        } catch (Exception e) {
            log.error("assignDrivers: Error during assigning driver", e);
        }
    }

    private String getList(List<Trip> tripList) {
        return tripList.stream().map(Trip::getId).map(UUID::toString).collect(Collectors.joining(","));
    }
}
