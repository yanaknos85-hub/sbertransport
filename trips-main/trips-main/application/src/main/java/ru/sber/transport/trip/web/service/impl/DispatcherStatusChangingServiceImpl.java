package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.Shift;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trip.web.service.DispatcherStatusChangingService;
import ru.sber.transport.trip.web.service.VerificationService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatcherStatusChangingServiceImpl implements DispatcherStatusChangingService {

    private final DriverProvider driverProvider;

    private final DriverSender driverSender;

    private final ShiftSender shiftSender;

    private final ShiftProvider shiftProvider;

    private final VerificationService verificationService;

    // Сопоставление статуса (ключа) и возможных переходов в этот статус из какого-либо (значения)
    private final Map<TripStatus, List<TripStatus>> possibleStatusesChanges = Map.of(
            TripStatus.DRIVER_ASSIGNED, List.of(TripStatus.WAITING_FOR_ASSIGNMENT),
            TripStatus.DRIVER_ON_THE_WAY, List.of(TripStatus.DRIVER_ASSIGNED),
            TripStatus.DRIVER_ARRIVED, List.of(TripStatus.DRIVER_ON_THE_WAY),
            TripStatus.TRIP_IN_PROGRESS, List.of(TripStatus.DRIVER_ARRIVED, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED),
            TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, List.of(TripStatus.TRIP_IN_PROGRESS),
            TripStatus.ORDER_FINISHED, List.of(TripStatus.TRIP_IN_PROGRESS),
            TripStatus.ORDER_CANCELLED_BY_CLIENT, List.of(TripStatus.WAITING_FOR_ASSIGNMENT, TripStatus.DRIVER_ASSIGNED,
                    TripStatus.DRIVER_ON_THE_WAY, TripStatus.DRIVER_ARRIVED),
            TripStatus.ORDER_CANCELLED_BY_DRIVER, List.of(TripStatus.WAITING_FOR_ASSIGNMENT, TripStatus.DRIVER_ASSIGNED,
                    TripStatus.DRIVER_ON_THE_WAY, TripStatus.DRIVER_ARRIVED),
            TripStatus.ORDER_EXPIRED, List.of(TripStatus.WAITING_FOR_ASSIGNMENT, TripStatus.DRIVER_ASSIGNED));

    @Override
    public Trip processStatusChangingByDispatcher(Trip trip, TripStatus status) {
        if (trip.getDriverId() != null) {
            var driver = driverProvider.get(trip.getDriverId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, trip.getDriverId()));
            var shift = shiftProvider.get(driver.getShiftId()).orElseThrow(() -> new EntityNotFoundException(Shift.class, driver.getShiftId()));
            var cancellationOfNonExecutableTrip = false;
            if(List.of(TripStatus.ORDER_CANCELLED_BY_CLIENT, TripStatus.ORDER_CANCELLED_BY_DRIVER, TripStatus.ORDER_EXPIRED).contains(status)
                    && !Objects.equals(driver.getActiveTripId(), trip.getId())) {
                verificationService.checkDriverToTripRelation(driverProvider.get(trip.getDriverId()).orElse(null), driver);
                cancellationOfNonExecutableTrip = true;
            } else {
                validateUpdate(trip, driver);
            }
            verificationService.checkPossibleStatus(possibleStatusesChanges.get(status), trip.getStatus());

            if (TripStatus.DRIVER_ARRIVED.equals(status)) {
                toDriverArrived(trip);
            } else if (TripStatus.TRIP_IN_PROGRESS.equals(status)) {
                toTripInProgress(trip, driver);
            } else if (TripStatus.ORDER_FINISHED.equals(status)) {
                toOrderFinished(trip, driver, shift);
            } else {
                toOtherStatus(driver, shift, status, cancellationOfNonExecutableTrip);
            }
        }
        trip.setStatus(status);
        return trip;
    }

    @Override
    public void validateUpdate(Trip trip, Driver driver) {
        verificationService.checkDriverToTripRelation(driverProvider.get(trip.getDriverId()).orElse(null), driver);
        verificationService.checkDriverBusynessForStatusChanging(trip, driver);
        if (driver.getActiveTripId() == null) {
            driver.setActiveTripId(trip.getId());
            log.debug("ActiveTripId changing! Driver = " + driver.getId() + "; Before = " + null + "; After = " + driver.getActiveTripId() + "; " + new Throwable().getStackTrace()[0].getFileName() + " " + new Throwable().getStackTrace()[0].getLineNumber());
            driverProvider.save(driver);
        }
    }

    private void toDriverArrived(Trip trip) {
        trip.setArrivedDate(OffsetDateTime.now(ZoneOffset.UTC));
    }

    private void toTripInProgress(Trip trip, Driver driver) {
        driver.setServing(true);
        if(trip.getFactStartTime() == null){
            trip.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        }
        trip.setStatus(TripStatus.TRIP_IN_PROGRESS);
        driverProvider.save(driver);
    }

    private void toOrderFinished(Trip trip, Driver driver, Shift shift) {
        trip.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setStatus(TripStatus.ORDER_FINISHED);
        processDriverAndShift(driver, shift, false);
    }

    private void toOtherStatus(Driver driver, Shift shift, TripStatus status, boolean cancellationOfNonExecutableTrip) {
        if (TripStatus.ORDER_CANCELLED_BY_CLIENT.equals(status) ||
                TripStatus.ORDER_CANCELLED_BY_DRIVER.equals(status) ||
                TripStatus.ORDER_EXPIRED.equals(status)) {
            processDriverAndShift(driver, shift, cancellationOfNonExecutableTrip);
        }
    }

    private void processDriverAndShift(Driver driver, Shift shift, boolean cancellationOfNonExecutableTrip) {
        if (!cancellationOfNonExecutableTrip) {
            driver.setServing(false);
            var before = driver.getActiveTripId();
            driver.setActiveTripId(null);
            log.debug("ActiveTripId changing! Driver = " + driver.getId() + "; Before = " + before + "; After = " + driver.getActiveTripId() + "; " + new Throwable().getStackTrace()[0].getFileName() + " " + new Throwable().getStackTrace()[0].getLineNumber());
            if (LocalDateTime.now(ZoneOffset.UTC).isAfter(shift.getEndDate())) {
                shift.setActive(false);
                shiftProvider.save(shift);
                shiftSender.send(shift);
                driver.setOnline(false);
                driver.setShiftId(null);
            }
            driverProvider.save(driver);
            driverSender.send(driver);
        }
    }
}
