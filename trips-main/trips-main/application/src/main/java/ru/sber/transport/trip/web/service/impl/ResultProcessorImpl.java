package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.dto.GetTripResponse;
import ru.sber.transport.trip.business.dto.Prefix;
import ru.sber.transport.trip.business.dto.TripV2Dto;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.database.trips.tables.records.ContractorsRecord;
import ru.sber.transport.trip.messaging.providers.*;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;
import ru.sber.transport.trip.web.service.ResultProcessor;

import java.util.*;
import java.util.function.Function;
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
            List<TripV2Dto> processedContent = toDto(page);
            return new PageImpl<>(
                    processedContent,
                    page.getPageable(),
                    page.getTotalElements()
            );
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
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TP,
                contractorProvider.getContractorDigitId(trip.getContractorId()), trip.getDigitId()));
        return new GetTripResponse(true, tripMapper.toOrder(trip,
                driverProvider.get(trip.getDriverId()).orElse(null),
                vehicleProvider.get(trip.getVehicleId()).orElse(null)), null);

    }

    private TripV2Dto toDto(Trip trip) {
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TP,
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
                if (driver != null) {
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
            if (trip.getVehicleId() == null && trip.getExpectedVehicleId() != null) {
                vehicle = vehicleProvider.get(trip.getExpectedVehicleId()).orElse(null);
            } else {
                vehicle = vehicleProvider.get(trip.getVehicleId()).orElse(null);
            }
            return tripMapper.toDtoV2(trip,
                    driver,
                    dispatcherProvider.get(trip.getDispatcherId()).orElse(null),
                    vehicle);
        }

    }

    private List<TripV2Dto> toDto(Page<Trip> trips) {
        Map<UUID, Long> contractorDigitIdMap = getContractorDigitIdMap(trips);
        List<Shift> shifts = shiftProvider.getAllByIds(trips.stream()
                .filter(item -> item.getPlannedShiftId() != null).map(Trip::getPlannedShiftId).toList());
        Map<UUID, Shift> shiftMap = getShiftMap(shifts);
        Map<UUID, Driver> driverMap = getSDriverMap(trips, shifts);
        Map<UUID, Vehicle> vehicleMap = getVehicleMap(trips, shifts);
        Map<UUID, Dispatcher> dispatcherMap = getDispatcherMap(trips);

        List<TripV2Dto> tripV2DtoList = new ArrayList<>();
        for (Trip trip : trips) {
            trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TP,
                    contractorDigitIdMap.get(trip.getContractorId()), trip.getDigitId()));
            trip.setWaypoints(trip.getWaypoints().stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).collect(Collectors.toList())); //NOSONAR
            trip.setRequests(trip.getRequests().stream().map(tripMapper::applyTimeZone).collect(Collectors.toSet()));
            Driver driver = null;
            Vehicle vehicle = null;
            if (trip.getPlannedShiftId() != null) {
                var shift = shiftMap.get(trip.getPlannedShiftId());
                if (shift != null) {
                    driver = driverMap.get(shift.getDriverId());
                    if (driver != null) {
                        driver.setShiftId(shift.getId());
                    }
                    vehicle = vehicleMap.get(shift.getVehicleId());
                }
                tripV2DtoList.add(tripMapper.toDtoV2WithPlanningData(trip,
                        driver, dispatcherMap.get(trip.getDispatcherId()), vehicle));
            } else {
                driver = driverMap.get(trip.getDriverId());
                if (trip.getVehicleId() == null && trip.getExpectedVehicleId() != null) {
                    vehicle = vehicleMap.get(trip.getExpectedVehicleId());
                } else {
                    vehicle = vehicleMap.get(trip.getVehicleId());
                }
                tripV2DtoList.add(tripMapper.toDtoV2(trip,
                        driver, dispatcherMap.get(trip.getDispatcherId()), vehicle));
            }
        }
        return tripV2DtoList;
    }

    private Map<UUID, Long> getContractorDigitIdMap(Page<Trip> trips) {
        List<UUID> contractorIds = trips.stream().map(Trip::getContractorId).filter(Objects::nonNull).distinct().toList();
        return contractorProvider.getContractorsByIds(contractorIds)
                .stream()
                .collect(Collectors.toMap(ContractorsRecord::getId, item -> item.getDigitId().longValue()));
    }

    private Map<UUID, Shift> getShiftMap(List<Shift> shifts) {
        return shifts.stream()
                .collect(Collectors.toMap(Shift::getId, Function.identity()));
    }

    private Map<UUID, Driver> getSDriverMap(Page<Trip> trips, List<Shift> shifts){
        List<UUID> driverIds = trips.stream().map(Trip::getDriverId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        driverIds.addAll(shifts.stream().map(Shift::getDriverId).filter(Objects::nonNull).distinct().toList());

        return driverProvider.getAllByIds(driverIds)
                .stream()
                .collect(Collectors.toMap(Driver::getId, Function.identity()));
    }

    private Map<UUID, Vehicle> getVehicleMap(Page<Trip> trips, List<Shift> shifts){
        List<UUID> vehicleIds = trips.stream().map(trip -> Optional.ofNullable(trip.getVehicleId()).orElse(trip.getExpectedVehicleId()))
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        vehicleIds.addAll(shifts.stream().map(Shift::getVehicleId).filter(Objects::nonNull).distinct().toList());

        return vehicleProvider.getAllByIds(vehicleIds)
                .stream()
                .collect(Collectors.toMap(Vehicle::getId, Function.identity()));
    }

    private Map<UUID, Dispatcher> getDispatcherMap(Page<Trip> trips){
        return dispatcherProvider.getAllByIds(trips.stream()
                        .map(Trip::getDispatcherId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Dispatcher::getId, Function.identity()));
    }
}
