package ru.sber.transport.trip.providers.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.dispatcher.messages.VehicleMessage;
import ru.sber.transport.trip.business.model.Vehicle;
import ru.sber.transport.trip.database.trips.tables.records.VehicleRecord;

@Mapper
public interface VehicleMapper {

    Vehicle toModel(VehicleMessage vehicleMessage);

    Vehicle toModel(VehicleRecord vehicleRecord);

    VehicleRecord toRecord(Vehicle vehicle);

}
