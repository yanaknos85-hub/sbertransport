package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.dto.FinalInfoTripDto;
import ru.sber.transport.trip.business.dto.RequestSearchDto;
import ru.sber.transport.trip.business.dto.TripFactInfoDto;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.ChannelType;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trip.web.service.RequestService;
import ru.sber.transport.trip.web.service.UpdateTripService;
import ru.sber.transport.trip.web.service.VerificationService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class RequestServiceImpl implements RequestService {

    private final TripProvider tripProvider;

    private final DriverProvider driverProvider;

    private final UpdateTripService updateTripService;

    private final List<TripSender> tripSenders;

    private final VerificationService verificationService;

    private final TripHistoryProvider tripHistoryProvider;

    @Override
    public Iterable<Trip> find(UUID contractorId, UUID dispatcherId, List<TripStatus> statuses, RequestSearchDto searchDto) {
        return tripProvider.findAllByContractorIdAndDispatcherIdAndStatusIn(contractorId, dispatcherId, statuses, searchDto);
    }

    @Override
    public Iterable<Trip> findByDriver(UUID driverId, List<TripStatus> statuses, RequestSearchDto searchDto) throws NoSuchFieldException {
        return tripProvider.findAllByDriverAndStatusIn(driverId, statuses, searchDto);
    }

    @Override
    public Trip find(UUID contractorId, UUID tripId, boolean withHistory) {
        var trip = tripProvider.findByContractorIdAndId(contractorId, tripId)
                .orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
        if (withHistory) {
            var history = tripHistoryProvider.getHistoryByTripId(tripId);
            var dispatcherTakeToWorkTime = history.stream().filter(h -> h.getAction().equals(ActionType.DISPATCHER_TAKE_TO_WORK)).findFirst();
            var statusChangingTime = history.stream().filter(h -> h.getAction().equals(ActionType.STATUS_CHANGING)).findFirst();
            dispatcherTakeToWorkTime.ifPresent(tripHistoryItem -> trip.setDispatcherTakeToWork(tripHistoryItem.getChangeTime().atOffset(ZoneOffset.UTC)));
            statusChangingTime.ifPresent(tripHistoryItem -> trip.setStatusChangedAt(tripHistoryItem.getChangeTime().atOffset(ZoneOffset.UTC)));
        }
        return trip;
    }

    @Override
    public Optional<Trip> find(UUID contractorId, String humanReadableId) {
        return tripProvider.findByContractorIdAndHumanReadableId(contractorId, humanReadableId);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public void updateFinal(UUID contractorId, UUID driverId, UUID tripId, FinalInfoTripDto tripDto) {
        var trip = tripProvider.get(tripId).orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
        var tripDriver = driverProvider.get(trip.getDriverId()).orElse(null);
        var currentDriver = driverProvider.get(driverId).orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
        verificationService.checkDriverToTripRelation(tripDriver, currentDriver);
        verificationService.checkDriverToContractorRelation(currentDriver, trip);
        var desiredStatuses = List.of(TripStatus.DRIVER_ARRIVED, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED);
        var desiredCheckinData = tripDto.getTripFactInfo().stream()
                .filter(tripFactInfoDto -> desiredStatuses.contains(tripFactInfoDto.status())).toList();
        if (!desiredCheckinData.isEmpty()) {
            long waitingTime = 0L;
            for (var tripFactData : desiredCheckinData) {
                waitingTime += tripFactData.endTime() - tripFactData.startTime();
            }
            trip.setDriverWaitingTime(Duration.ofMillis(waitingTime));
        }
        var passedDistance = tripDto.getTripFactInfo().stream().mapToDouble(TripFactInfoDto::passedDistance).sum();
        updateFinal(trip, tripDriver, passedDistance, false, Actor.DRIVER);
    }

    @Override
    public void updateFinal(UUID tripId, Map<String, Double> factDistances) {
        var tripOpt = tripProvider.get(tripId);
        if(tripOpt.isEmpty()) {
            log.error("Trip with id {} not found, fact distance message can not be processed", tripId);
            return;
        }
        var trip = tripOpt.get();
        var driverOpt = driverProvider.get(trip.getDriverId());
        if(driverOpt.isEmpty()) {
            log.error("Driver with id {} not found, fact distance message can not be processed", trip.getDriverId());
            return;
        }
        var driver = driverOpt.get();
        if (trip.getExpectedDistance() == null) {
            if (factDistances.containsKey("FORMULA")) {
                updateFinal(trip, driver, factDistances.get("FORMULA"), true, Actor.SYSTEM);
            }
            return;
        }

        var expectedDistance = trip.getExpectedDistance();
        var closestFactDistance = Double.MAX_VALUE;
        for (var factDistance : factDistances.values()) {
            if (Math.abs(expectedDistance - factDistance) < Math.abs(expectedDistance - closestFactDistance)) {
                closestFactDistance = factDistance;
            }
        }

        updateFinal(trip, driver, closestFactDistance, false, Actor.SYSTEM);
    }

    @Override
    public Optional<Trip> get(UUID requestId) {
        return tripProvider.get(requestId);
    }

    private void updateFinal(Trip trip, Driver driver, Double factDistance, boolean isFromFormula, Actor actor) {
        var planDistance = trip.getExpectedDistance();
        var curFactDistance = trip.getFactDistance();
        if (curFactDistance != null && planDistance != null
                && Math.abs(curFactDistance - planDistance) < Math.abs(factDistance - planDistance)) {
            return;
        }

        if (planDistance == null && !isFromFormula) {
            return;
        }

        trip.setFactDistance(factDistance);
        var historyItem = new TripHistoryItem(LocalDateTime.now(ZoneOffset.UTC), trip.getId(), trip.getDispatcherId(),
                trip.getDispatcherId(), trip.getDriverId(), trip.getDriverId(), trip.getStatus(), trip.getStatus(),
                trip.getDriverId(), actor, ActionType.FACT_DATA_CHANGING, null, null, null, null);
        tripProvider.updateFactData(trip);
        var savedTrip = tripProvider.get(trip.getId()).orElseThrow(() -> new EntityNotFoundException(Trip.class, trip.getId()));
        tripSenders.forEach(sender -> sender.send(savedTrip, false, ChannelType.KAFKA));
        tripHistoryProvider.save(historyItem);
    }
}
