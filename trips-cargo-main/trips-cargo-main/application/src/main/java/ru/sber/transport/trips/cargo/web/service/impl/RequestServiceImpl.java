package ru.sber.transport.trips.cargo.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trips.cargo.business.dto.EditTripDto;
import ru.sber.transport.trips.cargo.business.dto.FinalInfoTripDto;
import ru.sber.transport.trips.cargo.business.dto.RequestSearchDto;
import ru.sber.transport.trips.cargo.business.dto.TripFactInfoDto;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.ChannelType;
import ru.sber.transport.trips.cargo.messaging.providers.DispatcherProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trips.cargo.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trips.cargo.web.service.DispatcherStatusChangingService;
import ru.sber.transport.trips.cargo.web.service.RequestService;
import ru.sber.transport.trips.cargo.web.service.UpdateTripService;
import ru.sber.transport.trips.cargo.web.service.VerificationService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class RequestServiceImpl implements RequestService {

    private final TripProvider tripProvider;

    private final DriverProvider driverProvider;

    private final UpdateTripService updateTripService;

    private final DispatcherProvider dispatcherProvider;

    private final List<TripSender> tripSenders;

    private final DispatcherStatusChangingService dispatcherStatusChangingService;

    private final VerificationService verificationService;

    private final TripHistoryProvider tripHistoryProvider;

    @Override
    public Iterable<Trip> find(UUID contractorId, UUID dispatcherId, List<TripStatus> statuses, RequestSearchDto searchDto) throws NoSuchFieldException {
        var page = PageRequest.of(searchDto.getPage(), searchDto.getSize(), Sort.by(Trip.class.getDeclaredField("startTime").getName()).ascending());
        return tripProvider.findAllByContractorIdAndDispatcherIdAndStatusIn(contractorId, dispatcherId, statuses, page, searchDto);
    }

    @Override
    public Iterable<Trip> findByDriver(UUID driverId, List<TripStatus> statuses, RequestSearchDto searchDto) throws NoSuchFieldException {
        return tripProvider.findAllByDriverAndStatusIn(driverId, statuses, searchDto);
    }

    @Override
    public Trip find(UUID contractorId, UUID tripId) {
        return tripProvider.findByContractorIdAndId(contractorId, tripId)
                .orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
    }

    @Override
    public Optional<Trip> find(UUID contractorId, String humanReadableId) {
        return tripProvider.findByContractorIdAndHumanReadableId(contractorId, humanReadableId);
    }

    @Override
    public void update(UUID contractorId, UUID dispatcherId, UUID tripId, EditTripDto tripDto) {
        var channels = new ArrayList<>(List.of(ChannelType.BOTH));
        var trip = tripProvider.get(tripId).orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
        var dispatcher = dispatcherProvider.get(dispatcherId)
                .orElseThrow(() -> new EntityNotFoundException(Dispatcher.class, dispatcherId));
        verificationService.checkDispatcherToContractorRelation(dispatcher, trip);
        var driver = driverProvider.get(trip.getDriverId()).orElse(null);
        var driverId = tripDto.driverId();
        if (driverId !=null) {
            driver = driverProvider.get(driverId).orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
            if (trip.getDriverId() != null && !driver.getId().equals(trip.getDriverId())) {
                var pastDriverId = trip.getDriverId();
                var pastDriver = driverProvider.get(pastDriverId).orElseThrow(() -> new EntityNotFoundException(Driver.class, pastDriverId));
                pastDriver.setServing(false);
                pastDriver.setActiveTripId(null);
                driverProvider.save(pastDriver);
                channels.add(ChannelType.KAFKA);
            }
        }
        Trip savedTrip;
        if(tripDto.status() != null) {
            verificationService.checkEndStatus(trip, tripDto.status());
            trip = dispatcherStatusChangingService.processStatusChangingByDispatcher(trip, tripDto.status());
            savedTrip = updateTripService.updateTrip(driver, trip, tripDto.status());
        } else {
            savedTrip = updateTripService.updateTrip(driver, trip, trip.getStatus());
        }
        tripSenders.forEach(sender -> sender.send(savedTrip, false, channels.toArray(ChannelType[]::new)));
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
        if(!desiredCheckinData.isEmpty()) {
            // надо будет допилить когда будут для этого требования, этот код пока бесполезен, но пока лучше не удалять
            long waitingTime = 0L;
            for (var tripFactData : desiredCheckinData) {
                waitingTime+= tripFactData.endTime()-tripFactData.startTime();
            }
        }
        var passedDistance = tripDto.getTripFactInfo().stream().mapToDouble(TripFactInfoDto::passedDistance).sum();
        var historyItem = new TripHistoryItem(LocalDateTime.now(ZoneOffset.UTC),trip.getId(),trip.getDispatcherId(),
                trip.getDispatcherId(), trip.getDriverId(), trip.getDriverId(), trip.getStatus(), trip.getStatus(),
                driverId, Actor.DRIVER, ActionType.FACT_DATA_CHANGING, null, null);
        trip.setFactDistance(passedDistance);
        var savedTrip = updateTripService.updateTrip(tripDriver, trip, trip.getStatus());
        tripSenders.forEach(sender -> sender.send(savedTrip, false, ChannelType.BOTH));
        tripHistoryProvider.save(historyItem);
    }

    @Override
    public Optional<Trip> get(UUID requestId) {
        return tripProvider.get(requestId);
    }
}
