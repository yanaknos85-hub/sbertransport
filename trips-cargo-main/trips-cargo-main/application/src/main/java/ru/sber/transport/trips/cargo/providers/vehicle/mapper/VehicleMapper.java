package ru.sber.transport.trips.cargo.providers.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.dispatcher.messages.VehicleMessage;
import ru.sber.transport.trips.cargo.business.dto.DriverOnMapDto;
import ru.sber.transport.trips.cargo.business.model.Vehicle;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.VehicleRecord;

@Mapper
public interface VehicleMapper {

    Vehicle toModel(VehicleMessage vehicleMessage);

    Vehicle toModel(VehicleRecord vehicleRecord);

    VehicleRecord toRecord(Vehicle vehicle);

}
