package ru.sber.transport.trip.business.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.trip.business.TripUseCases;
import ru.sber.transport.trip.business.dto.CreateTripResponse;
import ru.sber.transport.trip.business.dto.IntegrationRequestDTO;
import ru.sber.transport.trip.business.dto.Prefix;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.SrmProvider;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.ChannelType;
import ru.sber.transport.trip.messaging.providers.AutoparkProvider;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.messaging.senders.TripSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
class TripUseCasesImpl implements TripUseCases<Request> {

    private final TripProvider tripProvider;

    private final SrmProvider srmProvider;

    private final TripSender tripSender;

    private final DriverProvider driverProvider;

    private final ShiftProvider shiftProvider;

    private final DriverSender driverSender;

    private final ShiftSender shiftSender;

    private final ru.sber.transport.trip.messaging.senders.dispatcher.TripSender tripDispatcherSender;

    private final TripHistoryProvider tripHistoryProvider;

    private final TripMapper tripMapper;

    private final ContractorProvider contractorProvider;

    private final AutoparkProvider autoparkProvider;

    @Override
    @Async
    public void process(Request request, UUID driverId, UUID vehicleId) {
        var trip = createTripFunctions().get(request.isCoopTrip()).apply(request);
        log.debug("Saving trip");
        trip
                .thenApply(tr -> updateStatus(tr, request))
                .thenApply(tr -> {
                    tr.setDriverId(driverId);
                    tr.setVehicleId(vehicleId);
                    var isNew = tripProvider.get(tr.getId()).isEmpty();
                    if (isNew) {
                        tr.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
                    }
                    var stage = tripProvider.save(tr);
                    if (stage == 1) {
                        tripSender.send(tr);
                        if (isNew) {
                            tripDispatcherSender.send(tr, true, ChannelType.WEB_SOCKET);
                            tripHistoryProvider.save(new TripHistoryItem(
                                    LocalDateTime.now(ZoneOffset.UTC), tr.getId(), null, tr.getDispatcherId(),
                                    null, tr.getDriverId(), null, tr.getStatus(),
                                    tr.getRequests().stream()
                                            .sorted(Comparator.comparing(Request::getCreationTime))
                                            .collect(Collectors.toList()) //NOSONAR
                                            .get(0).getPassengerId(),
                                    Actor.PASSENGER, ActionType.TRIP_CREATION, null, null,
                                    null, null
                            ));
                        }
                    }
                    return null;
                }).exceptionally(e -> {
                    log.error("Saving failed", e);
                    return null;
                });
    }

    @Override
    public CreateTripResponse process(IntegrationRequestDTO request, UUID contractorId) {
        if (tripProvider.get(request.getRequestId()).isPresent()) {
            return createErrorResponse(HttpStatus.CONFLICT.value(),
                    "Trip " + request.getHumanReadableId() + " already exists");
        }
        var trip = new Trip();
        tripMapper.fillTrip(trip, request, contractorId);
        trip.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT);
        trip.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setId(request.getRequestId());
        trip.setExpectedEndTime(trip.getExpectedStartTime().plusSeconds(request.getExpected().getTime()));
        if(trip.getExpectedVehicleId() != null) {
            var shifts = shiftProvider.getShiftByVehicleIdAndCurrentDate(trip.getExpectedVehicleId(), trip.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
            if(!shifts.isEmpty()) {
                trip.setPlannedShiftId(shifts.get(0).getId());
            }
        }
        if(request.getDepartmentId()!=null) {
            var autoparkOpt = autoparkProvider.getByRoutingId(request.getDepartmentId());
            autoparkOpt.ifPresent(autoparkRecord -> trip.setAutoparkId(autoparkRecord.getId()));
        }
        var stage = tripProvider.save(trip);
        if (stage == 1) {
            tripSender.send(trip);
            tripDispatcherSender.send(trip, true, ChannelType.WEB_SOCKET);
        } else {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while saving route " + request.getHumanReadableId());
        }
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TP,
                contractorProvider.getContractorDigitId(trip.getContractorId()), trip.getDigitId()));
        return new CreateTripResponse(true,
                trip.getHumanReadableId(),
                trip.getId().toString(),
                null
        );
    }

    @Override
    public Trip updateStatus(Trip trip, Request request) {
        trip.getRequests().stream().filter(r -> r.getId().equals(request.getId()))
                .findFirst().ifPresent(r -> r.setStatus(request.getStatus()));

        var requestStatus = TripRequestStatus.valueOf(request.getStatus());
        var requestTripStatus = TripStatus.getByTripRequestStatus(requestStatus);

        if (trip.getStatus() != null && trip.getStatus().isTerminal()) {
            return trip;
        }

        if (!requestTripStatus.isTerminal()) {
            trip.setStatus(requestTripStatus);
            return trip;
        }

        if (isTripFinishable(trip, requestStatus)) {
            trip.setStatus(TripStatus.ORDER_FINISHED);
            return trip;
        }

        if (!isTripCancellable(requestStatus)) {
            return trip;
        }

        var isFirst = trip
                .getRequests()
                .stream()
                .map(Request.class::cast)
                .min(Comparator.comparing(Request::getCreationTime))
                .map(Request::getId)
                .filter(request.getId()::equals)
                .isPresent();

        if (isFirst) {
            trip.getRequests().forEach(r -> r.setStatus(request.getStatus()));
        }

        if (isRequestsTerminal(trip)) {
            trip.setStatus(TripStatus.ORDER_CANCELLED_BY_CLIENT);
            freeDriver(trip);
        }

        return trip;
    }

    @Override
    public CreateTripResponse processCancel(Trip trip) {
        if (trip.getStatus().isTerminal()) {
            return createErrorResponse(HttpStatus.CONFLICT.value(),
                    "Trip " + trip.getHumanReadableId() + " is already finished, cancel is impossible");
        }
        trip.setStatus(TripStatus.ORDER_CANCELLED_BY_CLIENT);
        trip.setPlannedShiftId(null);
        freeDriver(trip);
        var stage = tripProvider.save(trip);
        if (stage == 1) {
            tripSender.send(trip);
            tripDispatcherSender.send(trip, false, ChannelType.WEB_SOCKET);
        } else {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while cancelling route " + trip.getHumanReadableId());
        }
        return new CreateTripResponse(true,
                trip.getHumanReadableId(),
                trip.getId().toString(),
                null
        );
    }

    private boolean isTripCancellable(TripRequestStatus requestStatus) {
        return TripRequestStatus.TAXI_CANCELLED.equals(requestStatus)
                || TripRequestStatus.GROUP_TRANSFER_CANCELLED.equals(requestStatus);
    }

    private boolean isTripFinishable(Trip trip, TripRequestStatus requestStatus) {
        return (TripRequestStatus.TAXI_TRIP_FINISHED.equals(requestStatus)
                || TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED.equals(requestStatus))
                && isRequestsTerminal(trip);
    }

    private boolean isRequestsTerminal(Trip trip) {
        return trip
                .getRequests()
                .stream()
                .map(Request.class::cast)
                .map(Request::getStatus)
                .allMatch(status -> TripRequestStatus.valueOf(status).isTerminal());
    }

    private Map<Boolean, Function<Request, CompletableFuture<Trip>>> createTripFunctions() {
        return Map.of(
                true, r -> createCoopTrip(r, r.getRideId()),
                false, r -> createTrip(r, r.getId())
        );
    }

    private CompletableFuture<Trip> createCoopTrip(Request request, UUID id) {
        var trip = tripProvider.get(id).orElseGet(Trip::new);
        if (trip.getStatus().ordinal() < TripStatus.TRIP_IN_PROGRESS.ordinal()) {
            if (trip.getRequests().isEmpty()) {
                return createTrip(request, id);
            } else {
                trip.setId(id);
                trip.setTaxiClass(getTaxiClass(request));
                trip.setContractorId(request.getContractorId());
                setFactData(trip, request);
                return requestCoop(trip);
            }
        } else {
            setFactData(trip, request);
            return completeTrip(trip);
        }
    }

    private CompletableFuture<Trip> createTrip(Request request, UUID id) {
        var expected = request.getExpected();

        var startTime = request.getDesiredDate();
        var endTime = startTime.plus(expected.time());

        var trip = tripProvider.get(id).orElseGet(Trip::new);
        if (trip.getId() == null) {
            trip.setId(id);
        }
        trip.setExpectedEndTime(endTime);
        trip.setExpectedStartTime(startTime);
        trip.setWaypoints(request.getWaypoints());
        trip.setPassengerCount(request.getPassengerCount());
        trip.setDriverWaitingTime(request.getDriverWaitingTime());
        trip.setTaxiClass(getTaxiClass(request));
        trip.setContractorId(request.getContractorId());
        trip.setRequests(new HashSet<>(Set.of(request)));
        trip.setFactDistance(request.getFactDistance());
        trip.setExpectedCost(request.getExpected() != null ? request.getExpected().cost() : null);
        trip.setExpectedDistance(request.getExpected() != null ? request.getExpected().distance() : null);
        trip.setExpectedTime(Duration.between(startTime, endTime).toSeconds());
        return completeTrip(trip);
    }

    private CompletableFuture<Trip> requestCoop(Trip trip) {
        return srmProvider.update(trip);
    }

    @NonNull
    private CompletableFuture<Trip> completeTrip(Trip trip) {
        var completable = new CompletableFuture<Trip>();
        completable.complete(trip);
        return completable;
    }

    private void setFactData(Trip trip, Request request) {
        trip.getRequests().removeIf(r -> r.equals(request));
        trip.setRequests(Stream.concat(Optional.ofNullable(trip.getRequests()).orElseGet(HashSet::new).stream(),
                Stream.of(request)).collect(Collectors.toSet()));
        trip.setDriverWaitingTime(request.getDriverWaitingTime());
        trip.setFactDistance(request.getFactDistance());
    }

    private void freeDriver(Trip trip) {
        var driverOptional = driverProvider.get(trip.getDriverId());
        if (driverOptional.isPresent()
                && driverOptional.get().getActiveTripId() != null
                && driverOptional.get().getActiveTripId().equals(trip.getId())) {
            var driver = driverOptional.get();
            var shiftOptional = shiftProvider.get(driver.getShiftId());
            driver.setServing(false);
            var before = driver.getActiveTripId();
            driver.setActiveTripId(null);
            log.debug("ActiveTripId changing! Driver = " + driver.getId() + "; Before = " + before + "; After = " + driver.getActiveTripId() + "; " + new Throwable().getStackTrace()[0].getFileName() + " " + new Throwable().getStackTrace()[0].getLineNumber());
            driverProvider.save(driver);
            if (shiftOptional.isPresent() && LocalDateTime.now(ZoneOffset.UTC).isAfter(shiftOptional.get().getEndDate())) {
                var shift = shiftOptional.get();
                shift.setActive(false);
                shiftProvider.save(shift);
                shiftSender.send(shift);
                driver.setOnline(false);
                driver.setShiftId(null);
                driverProvider.save(driver);
            }
        }
    }

    private CreateTripResponse createErrorResponse(int status, String message) {
        return new CreateTripResponse(false,
                null,
                null,
                new CreateTripResponse.Error(
                        status,
                        message
                ));
    }

    private String getTaxiClass(Request request) {
        if (TransportTypeEnum.GROUP_TRANSFER.name().equals(request.getTransportType())) {
            return request.getTransportType();
        } else return request.getTaxiClass();
    }

}
