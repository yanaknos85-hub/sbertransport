package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.dto.ShiftOperationType;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.ChannelType;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trip.web.service.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Component
@RequiredArgsConstructor
@Transactional
class DriverOnlineSwitcherServiceImpl implements DriverOnlineSwitcherService {

    private final DriverProvider driverProvider;

    private final ShiftProvider shiftProvider;

    private final UpdateTripServiceImpl updateTripService;

    private final TripProvider tripProvider;

    private final TripService tripService;

    private final VerificationService verificationService;

    private final ShiftService shiftService;

    private final TripSender tripSender;

    private final TripHistoryProvider tripHistoryProvider;

    private final AuthCheckService authCheckService;

    @SuppressWarnings("java:S3958")
    @Override
    public void switchOnline(UUID driverId, Authentication authentication) {
        var dispatcher = authCheckService.dispatcherAuthCheck(null, (JwtAuthenticationToken) authentication);
        var driver = driverProvider.get(driverId).orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
        if (dispatcher != null) {
            verificationService.checkDriverToDispatcherRelation(driver, dispatcher);
        }
        if (!driver.isOnline()) {
            doSwitchOnline(driver);
        } else {
            doSwitchOffline(driver, dispatcher == null ? UUID.fromString(((JwtAuthenticationToken) authentication).getToken().getId()) : dispatcher.getId(), dispatcher == null);
        }
    }

    private void doSwitchOffline(Driver driver, UUID dispatcherId, boolean isAdmin) {
        if (driver.getShiftId() != null) {
            var shift = shiftProvider.get(driver.getShiftId()).orElseThrow(() ->
                    new EntityNotFoundException(Shift.class, driver.getShiftId()));
            verificationService.checkManualOnlineSwitchAvailable(shift);
            verificationService.checkShiftIsDeleted(shift, ShiftOperationType.EXIT);
            verificationService.checkDriverBusynessForExitFromShift(driver);
            var trips = tripProvider.findAllByDriverAndStatusIn(driver.getId(), List.of(TripStatus.DRIVER_ASSIGNED));
            trips.parallelStream().forEach(trip -> {
                var historyItem = new TripHistoryItem();
                historyItem.setChangeTime(LocalDateTime.now(ZoneOffset.UTC));
                historyItem.setTripId(trip.getId());
                historyItem.setOldDispatcherId(trip.getDispatcherId());
                historyItem.setOldDriverId(trip.getDriverId());
                historyItem.setOldStatus(trip.getStatus());
                historyItem.setActorId(dispatcherId);
                historyItem.setActorType(isAdmin ? Actor.ADMIN : Actor.DISPATCHER);
                historyItem.setAction(ActionType.DRIVER_CHANGING);

                trip.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT);
                trip.setDriverId(null);
                trip.setVehicleId(null);

                var updatedTrip = updateTripService.updateTrip(null, trip, trip.getStatus());

                historyItem.setNewDispatcherId(updatedTrip.getDispatcherId());
                historyItem.setNewDriverId(updatedTrip.getDriverId());
                historyItem.setNewStatus(updatedTrip.getStatus());
                tripSender.send(trip, false, ChannelType.BOTH);
                tripHistoryProvider.save(historyItem);
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
