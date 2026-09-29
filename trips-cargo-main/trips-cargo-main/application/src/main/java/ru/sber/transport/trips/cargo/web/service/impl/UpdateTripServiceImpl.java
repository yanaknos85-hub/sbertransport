package ru.sber.transport.trips.cargo.web.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.business.model.Shift;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ShiftProvider;
import ru.sber.transport.trips.cargo.web.service.UpdateTripService;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;

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

