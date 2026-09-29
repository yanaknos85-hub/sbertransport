package ru.sber.transport.trips.cargo.messaging.processor.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.messages.ShiftMessage;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trips.cargo.business.dto.EwbStatus;
import ru.sber.transport.trips.cargo.business.dto.ShiftOperationType;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.message.EwbMessage;
import ru.sber.transport.trips.cargo.messaging.ChannelType;
import ru.sber.transport.trips.cargo.messaging.processor.ShiftProcessor;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ShiftProvider;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trips.cargo.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trips.cargo.providers.shift.mapper.ShiftMapper;
import ru.sber.transport.trips.cargo.web.service.ShiftService;
import ru.sber.transport.trips.cargo.web.service.TripService;
import ru.sber.transport.trips.cargo.web.service.UpdateTripService;
import ru.sber.transport.trips.cargo.web.service.VerificationService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Component
@Slf4j
public class ShiftProcessorImpl implements ShiftProcessor {

    private final ShiftProvider shiftProvider;

    private final ShiftMapper shiftMapper;

    private final TripProvider tripProvider;

    private final UpdateTripService updateTripService;

    private final TripSender tripSender;

    private final ShiftSender shiftSender;

    private final DriverSender driverSender;

    private final VerificationService verificationService;

    private final DriverProvider driverProvider;

    private final TripHistoryProvider tripHistoryProvider;

    private final ShiftService shiftService;

    private final TripService tripService;

    @Override
    public Future<Integer> processShift(ShiftMessage message, Driver driver) {
        log.info("Saving shift data with id "+message.id());
        var existingShift = shiftProvider.get(message.id());
        if (existingShift.isEmpty()) {
            shiftProvider.save(shiftMapper.toModel(message));
        } else {
            if (existingShift.get().isActive()) {
                if (message.deleted() || !message.active()) {
                    var deleted = existingShift.get();
                    var changedTrips = new ArrayList<Trip>();
                    var trips = tripProvider.findAllByDriverId(driver.getId());
                    trips.parallelStream().forEach(trip -> {
                        if (TripStatus.DRIVER_ASSIGNED.equals(trip.getStatus())) {
                            trip.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT);
                            trip.setDriverId(null);
                            trip.setVehicleId(null);
                            changedTrips.add(trip);
                        }
                    });
                    changedTrips.forEach(trip -> {
                        updateTripService.updateTrip(null, trip, trip.getStatus());
                        tripSender.send(trip, false, ChannelType.BOTH);
                    });
                    trips.removeIf(trip -> TripStatus.ORDER_CANCELLED_BY_CLIENT.equals(trip.getStatus())
                            || TripStatus.ORDER_CANCELLED_BY_DRIVER.equals(trip.getStatus())
                            || TripStatus.ORDER_EXPIRED.equals(trip.getStatus())
                            || TripStatus.ORDER_FINISHED.equals(trip.getStatus())
                            || TripStatus.UNDEFINED.equals(trip.getStatus()));
                    if (trips.size() > changedTrips.size()) {
                        deleted.setEndDate(LocalDateTime.now(ZoneOffset.UTC));
                        shiftProvider.save(deleted);
                        shiftSender.send(deleted);
                        driverSender.send(driver);
                    } else {
                        verificationService.checkShiftIsDeleted(deleted, ShiftOperationType.DEACTIVATION);
                        deleted.setActive(message.active());
                        deleted.setDeleted(message.deleted());
                        shiftProvider.save(deleted);
                        releasePlannedTrips(deleted);
                        var driverFromShift = driverProvider.get(deleted.getDriverId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, deleted.getDriverId()));
                        driverFromShift.setShiftId(null);
                        driverFromShift.setOnline(false);
                        driverProvider.save(driverFromShift);
                    }
                } else {
                    shiftProvider.save(shiftMapper.toModel(message));
                }

                var driverFromShift = driverProvider.get(existingShift.get().getDriverId())
                        .orElseThrow(() -> new EntityNotFoundException(Driver.class, existingShift.get().getDriverId()));
                driverSender.send(driverFromShift);
            } else {
                var shift = shiftMapper.toModel(message);
                shiftProvider.save(shift);
                releasePlannedTrips(shift);
            }
        }
        log.info("Shift data saved");
        return AsyncResult.forValue(1);
    }

    @Override
    public void processEwbUpdate(EwbMessage message) {
        if(message.status() == null || message.status().isEmpty()){
            return;
        }
        if(Arrays.stream(EwbStatus.values()).map(EwbStatus::name).toList().contains(message.status())){
            switch (EwbStatus.valueOf(message.status())) {
                case ON_THE_LINE -> {
                    var shiftOptional = shiftProvider.getByEwbId(message.id());
                    if(shiftOptional.isPresent()) {
                        var shift = shiftOptional.get();
                        verificationService.checkShiftIsDeleted(shift, ShiftOperationType.ENTER);
                        shiftService.activate(shift);
                        tripService.processPlannedTrips(shift);
                    }
                }
                case EWB_CLOSED -> {
                    var shiftOptional = shiftProvider.getByEwbId(message.id());
                    if(shiftOptional.isPresent()) {
                        var shift = shiftOptional.get();
                        verificationService.checkShiftIsDeleted(shift, ShiftOperationType.EXIT);
                        var driverOpt = driverProvider.get(shift.getDriverId());
                        if(driverOpt.isEmpty()){
                            log.error("Водитель {} не найден", shift.getDriverId());
                            return;
                        }
                        var driver = driverOpt.get();
                        verificationService.checkDriverBusynessForExitFromShift(driver);
                        var trips = tripProvider.findAllByDriverAndStatusIn(driver.getId(), List.of(TripStatus.DRIVER_ASSIGNED));
                        trips.parallelStream().forEach(trip -> {
                            var historyItem = new TripHistoryItem();
                            historyItem.setChangeTime(LocalDateTime.now(ZoneOffset.UTC));
                            historyItem.setTripId(trip.getId());
                            historyItem.setOldDispatcherId(trip.getDispatcherId());
                            historyItem.setOldDriverId(trip.getDriverId());
                            historyItem.setOldStatus(trip.getStatus());
                            historyItem.setActorId(driver.getId());
                            historyItem.setActorType(Actor.EWB);
                            historyItem.setAction(ActionType.DRIVER_CHANGING);
                            trip.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT);
                            trip.setDriverId(null);
                            trip.setVehicleId(null);
                            var updatedTrip = updateTripService.updateTrip(null, trip, trip.getStatus());
                            tripSender.send(trip, false, ChannelType.BOTH);
                            historyItem.setNewDispatcherId(updatedTrip.getDispatcherId());
                            historyItem.setNewDriverId(updatedTrip.getDriverId());
                            historyItem.setNewStatus(updatedTrip.getStatus());
                            tripHistoryProvider.save(historyItem);
                        });
                        shiftService.deactivate(shift);
                    }
                }
                case EWB_CANCELLED -> {
                    var shiftOptional = shiftProvider.getByEwbId(message.id());
                    if(shiftOptional.isPresent()){
                        var shift = shiftOptional.get();
                        shift.setEwbId(null);
                        shiftProvider.save(shift);
                        shiftSender.send(shift);
                    }
                }
            }
        }
    }

    private void releasePlannedTrips(Shift shift) {
        if(shift.isDeleted()){
            var trips = tripProvider.findAllByPlannedShiftId(shift.getId());
            trips.forEach(trip -> {
                var historyItem = new TripHistoryItem();
                historyItem.setChangeTime(LocalDateTime.now(ZoneOffset.UTC));
                historyItem.setTripId(trip.getId());
                historyItem.setOldDispatcherId(trip.getDispatcherId());
                historyItem.setNewDispatcherId(trip.getDispatcherId());
                historyItem.setOldDriverId(trip.getDriverId());
                historyItem.setNewDriverId(trip.getDriverId());
                historyItem.setOldStatus(trip.getStatus());
                historyItem.setNewStatus(trip.getStatus());
                historyItem.setOldPlannedShiftId(trip.getPlannedShiftId());
                historyItem.setNewPlannedShiftId(null);
                historyItem.setActorId(null);
                historyItem.setAction(ActionType.TRIP_PLANNING_CANCELLATION);
                historyItem.setActorType(Actor.SYSTEM);

                trip.setPlannedShiftId(null);
                updateTripService.updateTrip(driverProvider.get(trip.getDriverId()).orElse(null), trip, trip.getStatus());
                tripHistoryProvider.save(historyItem);
            });
        }
    }

}
