package ru.sber.transport.trips.cargo.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.business.model.Shift;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ShiftProvider;
import ru.sber.transport.trips.cargo.web.service.DispatcherStatusChangingService;
import ru.sber.transport.trips.cargo.web.service.VerificationService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class DispatcherStatusChangingServiceImpl implements DispatcherStatusChangingService {

    private final DriverProvider driverProvider;

    private final ShiftProvider shiftProvider;

    private final VerificationService verificationService;

    @Override
    public Trip processStatusChangingByDispatcher(Trip trip, TripStatus status){
        if(trip.getDriverId()!=null){
            var driver = driverProvider.get(trip.getDriverId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, trip.getDriverId()));
            var shift = shiftProvider.get(driver.getShiftId()).orElseThrow(() -> new EntityNotFoundException(Shift.class, driver.getShiftId()));
            validateUpdate(trip, driver);
            if (TripStatus.DRIVER_ARRIVED.equals(status)) {
                toDriverArrived(trip);
            } else if (TripStatus.TRIP_IN_PROGRESS.equals(status)) {
                toTripInProgress(trip, driver);
            } else if (TripStatus.ORDER_FINISHED.equals(status)) {
                toOrderFinished(trip, driver, shift);
            } else {
                toOtherStatus(driver, shift, status);
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
            driverProvider.save(driver);
        }
    }

    private void toDriverArrived(Trip trip){
        trip.setArrivedDate(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
    }

    private void toTripInProgress(Trip trip, Driver driver){
        driver.setServing(true);
        if(trip.getStartTime() == null){
            trip.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        }
        trip.setStatus(TripStatus.TRIP_IN_PROGRESS);
        driverProvider.save(driver);
    }

    private void toOrderFinished(Trip trip, Driver driver, Shift shift){
        trip.setEndTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip.setStatus(TripStatus.ORDER_FINISHED);
        driver.setActiveTripId(null);
        driver.setServing(false);
        driverProvider.save(driver);
        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(shift.getEndDate())) {
            shift.setActive(false);
            shiftProvider.save(shift);
            driver.setOnline(false);
            driver.setShiftId(null);
            driverProvider.save(driver);
        }
    }

    private void toOtherStatus(Driver driver, Shift shift, TripStatus status){
        if (TripStatus.ORDER_CANCELLED_BY_CLIENT.equals(status) ||
                TripStatus.ORDER_CANCELLED_BY_DRIVER.equals(status) ||
                TripStatus.ORDER_EXPIRED.equals(status)) {
            driver.setServing(false);
            driver.setActiveTripId(null);
            driverProvider.save(driver);
            if (LocalDateTime.now(ZoneOffset.UTC).isAfter(shift.getEndDate())) {
                shift.setActive(false);
                shiftProvider.save(shift);
                driver.setOnline(false);
                driver.setShiftId(null);
                driverProvider.save(driver);
            }
        }
    }
}
