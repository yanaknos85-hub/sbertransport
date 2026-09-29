package ru.sber.transport.trips.cargo.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.sber.transport.trips.cargo.business.dto.GetTripResponse;
import ru.sber.transport.trips.cargo.business.dto.Prefix;
import ru.sber.transport.trips.cargo.business.dto.TripV2Dto;
import ru.sber.transport.trips.cargo.business.model.CargoRequest;
import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.Vehicle;
import ru.sber.transport.trips.cargo.business.model.Waypoint;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DispatcherProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.VehicleProvider;
import ru.sber.transport.trips.cargo.messaging.providers.*;
import ru.sber.transport.trips.cargo.providers.trips.mapping.TripMapper;
import ru.sber.transport.trips.cargo.web.service.ResultProcessor;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ResultProcessorImpl implements ResultProcessor {

    private final ContractorProvider contractorProvider;

    private final DriverProvider driverProvider;

    private final DispatcherProvider dispatcherProvider;

    private final VehicleProvider vehicleProvider;

    private final ShiftProvider shiftProvider;

    private final TripMapper tripMapper;

    @Override
    public Iterable<TripV2Dto> process(Iterable<Trip> result) {
        if (result instanceof Page<Trip> page) {
            return page
                    .map(this::toDto);
        } else if (result instanceof Collection<Trip> collection) {
            return collection.parallelStream()
                    .map(this::toDto).toList();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public TripV2Dto process(Trip result) {
        return toDto(result);
    }

    @Override
    public GetTripResponse processForIntegration(Trip trip) {
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TC,
                contractorProvider.getContractorDigitId(trip.getContractorId()), trip.getDigitId()));
        return new GetTripResponse(true, tripMapper.toIntegrationResponse(trip,
                driverProvider.get(trip.getDriverId()).orElse(null),
                vehicleProvider.get(trip.getVehicleId()).orElse(null)), null);
    }

    private TripV2Dto toDto (Trip trip){
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TC,
                contractorProvider.getContractorDigitId(trip.getContractorId()), trip.getDigitId()));
        trip.setWaypoints(trip.getWaypoints().stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).collect(Collectors.toList())); //NOSONAR
        trip.setRequests(trip.getRequests().stream().map(tripMapper::applyTimeZone).collect(Collectors.toSet()));
        Driver driver = null;
        Vehicle vehicle = null;
        if (trip.getPlannedShiftId() != null) {
            var shift = shiftProvider.get(trip.getPlannedShiftId())
                    .orElse(null);
            if (shift != null) {
                driver = driverProvider.get(shift.getDriverId()).orElse(null);
                if(driver!=null) {
                    driver.setShiftId(shift.getId());
                }
                vehicle = vehicleProvider.get(shift.getVehicleId()).orElse(null);
            }
            return tripMapper.toDtoV2WithPlanningData(trip,
                    driver,
                    dispatcherProvider.get(trip.getDispatcherId()).orElse(null),
                    vehicle);
        } else {
            driver = driverProvider.get(trip.getDriverId()).orElse(null);
            vehicle = vehicleProvider.get(trip.getVehicleId()).orElse(null);
            return tripMapper.toDtoV2(trip,
                    driver,
                    dispatcherProvider.get(trip.getDispatcherId()).orElse(null),
                    vehicle);
        }
    }
}
