package ru.sber.transport.trips.cargo.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trips.cargo.business.dto.ShiftOperationType;
import ru.sber.transport.trips.cargo.business.model.Dispatcher;
import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.business.model.Shift;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.ChannelType;
import ru.sber.transport.trips.cargo.messaging.providers.DispatcherProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ShiftProvider;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trips.cargo.web.service.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
class DriverOnlineSwitcherServiceImpl implements DriverOnlineSwitcherService {

    private final DriverProvider driverProvider;

    private final ShiftProvider shiftProvider;

    private final UpdateTripServiceImpl updateTripService;

    private final TripProvider tripProvider;

    private final VerificationService verificationService;

    private final ShiftService shiftService;

    private final TripSender tripSender;

    private final TripService tripService;

    private final AuthCheckService authCheckService;

    @SuppressWarnings("java:S3958")
    @Override
    public void switchOnline(UUID driverId, Authentication authentication) {
        var dispatcher = authCheckService.dispatcherAuthCheck(null, (JwtAuthenticationToken) authentication);
        var driver = driverProvider.get(driverId).orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
        if(dispatcher != null) {
            verificationService.checkDriverToDispatcherRelation(driver, dispatcher);
        }
        if (!driver.isOnline()) {
            doSwitchOnline(driver);
        } else {
            doSwitchOffline(driver);
        }
    }

    private void doSwitchOffline(Driver driver) {
        if (driver.getShiftId() != null) {
            var shift = shiftProvider.get(driver.getShiftId()).orElseThrow(() ->
                    new EntityNotFoundException(Shift.class, driver.getShiftId()));
            verificationService.checkManualOnlineSwitchAvailable(shift);
            verificationService.checkShiftIsDeleted(shift, ShiftOperationType.EXIT);
            verificationService.checkDriverBusynessForExitFromShift(driver);
            var trips = tripProvider.findAllByDriverAndStatusIn(driver.getId(), List.of(TripStatus.DRIVER_ASSIGNED));
            trips.parallelStream().forEach(trip -> {
                trip.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT);
                trip.setDriverId(null);
                trip.setVehicleId(null);
                updateTripService.updateTrip(null, trip, trip.getStatus());
                tripSender.send(trip, false, ChannelType.BOTH);
            });
            shiftService.deactivate(shift);
        }
    }

    private void doSwitchOnline(Driver driver) {
        var shifts = shiftProvider.getShiftByDriverIdAndCurrentDate(driver.getId(), LocalDateTime.now(ZoneOffset.UTC))
                .stream().sorted(Comparator.comparing(Shift::getStartDate)).toList();
        verificationService.checkShiftsExistence(shifts);
        var shift = shifts.get(0);
        verificationService.checkManualOnlineSwitchAvailable(shift);
        verificationService.checkShiftIsDeleted(shift, ShiftOperationType.ENTER);
        shiftService.activate(shift);
        tripService.processPlannedTrips(shift);
    }

}
