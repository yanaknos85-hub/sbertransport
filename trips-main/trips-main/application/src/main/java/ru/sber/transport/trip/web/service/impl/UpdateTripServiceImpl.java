package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.Shift;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.web.service.UpdateTripService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateTripServiceImpl implements UpdateTripService {

    private final ShiftProvider shiftProvider;

    private final TripProvider tripProvider;

    @Override
    public Trip updateTrip(Driver driver, Trip trip, TripStatus status) {
        var shift = new Shift();
        if (driver != null && driver.getShiftId() != null) {
            shift = shiftProvider.get(driver.getShiftId()).orElseThrow(() -> new EntityNotFoundException(Shift.class, driver.getShiftId()));
        }
        var finalShift = shift;
        trip.setDriverId(driver!= null ? driver.getId() : null);
        trip.setStatus(status);
        if (driver != null && finalShift.getId() != null) {
            trip.setVehicleId(finalShift.getVehicleId());
        }
        trip.setAutoassignCounter(0);
        tripProvider.save(trip);
        return tripProvider.get(trip.getId()).orElse(null);
    }

    @Override
    public Trip incrementAutoAssignCounter(Trip trip) {
        trip.setAutoassignCounter(trip.getAutoassignCounter() + 1);
        tripProvider.save(trip);
        return tripProvider.get(trip.getId()).orElse(null);
    }
}

