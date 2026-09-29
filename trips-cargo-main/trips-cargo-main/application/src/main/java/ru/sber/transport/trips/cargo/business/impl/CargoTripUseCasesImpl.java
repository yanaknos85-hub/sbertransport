package ru.sber.transport.trips.cargo.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.trips.cargo.business.TripUseCases;
import ru.sber.transport.trips.cargo.business.dto.CreateTripResponse;
import ru.sber.transport.trips.cargo.business.dto.IntegrationRequestDTO;
import ru.sber.transport.trips.cargo.business.dto.Prefix;
import ru.sber.transport.trips.cargo.business.mapper.TripUpdater;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.ChannelType;
import ru.sber.transport.trips.cargo.messaging.providers.AutoparkProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ShiftProvider;
import ru.sber.transport.trips.cargo.messaging.senders.TripSender;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.ShiftSender;
import ru.sberbank.ditsib.transport.request.messaging.RouteMessage;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
class CargoTripUseCasesImpl implements TripUseCases<RouteMessage> {

    private final TripProvider tripProvider;

    private final TripSender tripSender;

    private final TripUpdater tripUpdater;

    private final ContractorProvider contractorProvider;

    private final ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender tripDispatcherSender;

    private final DriverProvider driverProvider;

    private final ShiftProvider shiftProvider;

    private final ShiftSender shiftSender;

    private final AutoparkProvider autoparkProvider;

    @Override
    public void process(RouteMessage route, UUID driverId, UUID vehicleId) {
        var trip = tripProvider.get(route.id()).orElseGet(Trip::new);
        tripUpdater.update(trip, route);
        if (trip.getStatus() == null || !trip.getStatus().isTerminal()) {
            trip.setStatus(getStatus(route, trip.getStatus()));
            if(trip.getStatus().equals(TripStatus.ORDER_CANCELLED_BY_CLIENT)){
                freeDriver(trip);
            }
        }
        var isNew = tripProvider.get(trip.getId()).isEmpty();
        if (isNew) {
            trip.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        }
        var stage = tripProvider.save(trip);
        if (stage == 1) {
            tripSender.send(trip);
            if (isNew) {
                tripDispatcherSender.send(trip, true, ChannelType.WEB_SOCKET);
            }
        }
    }

    @Override
    public CreateTripResponse process(IntegrationRequestDTO request, UUID contractorId) {
        if (tripProvider.getByRouteHumanReadableId(request.getHumanReadableId()).isPresent()) {
            return createErrorResponse(HttpStatus.CONFLICT.value(),
                    "Route " + request.getHumanReadableId() + " already exists");
        }
        var trip = new Trip();
        tripUpdater.fillTrip(trip, request, contractorId);
        trip.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT);
        trip.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setId(UUID.randomUUID());
        var autoparkOpt = autoparkProvider.getByRoutingId(request.getDepartmentId());
        autoparkOpt.ifPresent(autoparkRecord -> trip.setAutoparkId(autoparkRecord.getId()));
        var stage = tripProvider.save(trip);
        if (stage == 1) {
            tripSender.send(trip);
            tripDispatcherSender.send(trip, true, ChannelType.WEB_SOCKET);
        } else {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while saving route " + request.getHumanReadableId());
        }
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TC,
                contractorProvider.getContractorDigitId(trip.getContractorId()), trip.getDigitId()));
        return new CreateTripResponse(true,
                trip.getHumanReadableId(),
                trip.getRouteHumanReadableId(),
                null
        );
    }

    @Override
    public CreateTripResponse processCancel(Trip trip) {
        if(trip.getStatus().isTerminal()){
            return createErrorResponse(HttpStatus.CONFLICT.value(),
                    "Trip "+trip.getHumanReadableId()+" is already finished, cancel is impossible");
        }
        trip.setStatus(TripStatus.ORDER_CANCELLED_BY_CLIENT);
        freeDriver(trip);
        tripProvider.save(trip);
        return new CreateTripResponse(true,
                trip.getHumanReadableId(),
                trip.getId().toString(),
                null
        );
    }

    @NotNull
    private TripStatus getStatus(RouteMessage route, TripStatus defaultValue) {
        return switch (route.status()) {
            case "CARGO_AWAITING_DATA" -> TripStatus.SENT_TO_CONTRACTOR;
            case "CARGO_AWAITING_TRANSFER" -> TripStatus.DRIVER_ASSIGNED;
            case "CARGO_TRANSFER_FINISHED" -> TripStatus.TRIP_IN_PROGRESS;
            case "CARGO_SHIPMENT_FINISHED", "CARGO_DELIVERY_CONFIRMATION_FINISHED" -> TripStatus.ORDER_FINISHED;
            case "CARGO_CANCELED" -> TripStatus.ORDER_CANCELLED_BY_CLIENT;
            default -> defaultValue;
        };
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


}
