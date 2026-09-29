package ru.sber.transport.trips.cargo.providers.shift.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.dispatcher.messages.ShiftMessage;
import ru.sber.transport.trips.cargo.business.dto.DriverOnMapDto;
import ru.sber.transport.trips.cargo.business.dto.ShiftResponseDTO;
import ru.sber.transport.trips.cargo.business.model.Shift;
import ru.sber.transport.trips.cargo.business.model.Vehicle;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.ShiftRecord;

@Mapper
public interface ShiftMapper {

    Shift toModel(ShiftMessage shiftMessage);

    Shift toModel(ShiftRecord shiftRecord);

    ShiftRecord toRecord(Shift shift);

    ShiftMessage toMessage(Shift shift);

    @Mapping(target = "id", source = "shift.id")
    ShiftResponseDTO toShiftResponseDTO(Shift shift, Vehicle vehicle);

    DriverOnMapDto.CurrentShift toOnMapShiftDto(Shift shift);

}
