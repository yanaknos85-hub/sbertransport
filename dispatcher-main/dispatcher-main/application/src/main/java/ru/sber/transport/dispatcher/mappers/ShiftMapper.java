package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.messages.CreateEwbMessage;
import ru.sber.transport.dispatcher.messages.ShiftFromMaisMessage;
import ru.sber.transport.dispatcher.messages.ShiftMessage;

import java.util.UUID;

@Mapper
public interface ShiftMapper {

    @Mapping(target = "contractorId", source = "contractorId")
    @Mapping(target = "driver.id", source = "shiftDTO.driverId")
    @Mapping(target = "vehicle.id", source = "shiftDTO.vehicleId")
    Shift toModel(ShiftDTO shiftDTO, UUID contractorId);

    @Mapping(target = "driver.passport", ignore = true)
    @Mapping(target = "driver.contactPhone", ignore = true)
    @Mapping(target = "driver.rating", ignore = true)
    @Mapping(target = "driver.driverLicenseNumber", ignore = true)
    @Mapping(target = "driver.serviceLicenseNumber", ignore = true)
    @Mapping(target = "driver.experience", ignore = true)
    @Mapping(target = "driver.attributes", ignore = true)
    @Mapping(target = "driver.driverLicenses", ignore = true)
    @Mapping(target = "vehicle.active", ignore = true)
    @Mapping(target = "vehicle.vin", ignore = true)
    @Mapping(target = "vehicle.passport", ignore = true)
    @Mapping(target = "vehicle.insuranceNumber", ignore = true)
    @Mapping(target = "vehicle.model.year", ignore = true)
    @Mapping(target = "vehicle.ecoClass", ignore = true)
    @Mapping(target = "vehicle.fuelConsumption", ignore = true)
    @Mapping(target = "vehicle.packageClass", ignore = true)
    @Mapping(target = "vehicle.mileage", ignore = true)
    @Mapping(target = "vehicle.manufactureYear", ignore = true)
    @Mapping(target = "vehicle.maxAllowedWeight", ignore = true)
    @Mapping(target = "vehicle.chassisType", ignore = true)
    @Mapping(target = "vehicle.transmissionType", ignore = true)
    @Mapping(target = "vehicle.bodyType", ignore = true)
    @Mapping(target = "vehicle.engineType", ignore = true)
    @Mapping(target = "vehicle.inExploitation", ignore = true)
    @Mapping(target = "vehicle.autopark", ignore = true)
    @Mapping(target = "vehicle.vehicleAdditional", ignore = true)
    ShiftResponseDTO toResponseDTO(Shift shift);

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "rowId", ignore = true)
    @Mapping(target = "shift.vehicle.id", source = "shiftDTO.vehicleId")
    @Mapping(target = "shift.driver.id", source = "shiftDTO.driverId")
    void update(@MappingTarget Shift shift, ShiftDTO shiftDTO);

    void update(@MappingTarget Shift shift, ShiftMessage shiftMessage);

    @Mapping(target = "driverId", source = "shift.driver.id")
    @Mapping(target = "vehicleId", source = "shift.vehicle.id")
    ShiftDTO toDTO(Shift shift);

    @Mapping(target = "driverId", source = "shift.driver.id")
    @Mapping(target = "vehicleId", source = "shift.vehicle.id")
    ShiftMessage toMessage(Shift shift);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driver.id", source = "driverId")
    @Mapping(target = "vehicle.id", source = "vehicleId")
    @Mapping(target = "contractorId", source = "contractorId")
    void update(@MappingTarget Shift shift, ShiftFromMaisMessage shiftFromMaisMessage, UUID driverId, UUID vehicleId, UUID contractorId);

    @Mapping(target = "startDate", expression = "java(dto.getStartDate().toLocalDate())")
    @Mapping(target = "finishDate", expression = "java(dto.getFinishDate().toLocalDate())")
    @Mapping(target = "driverEmployeeId", source = "dto.driverId")
    CreateEwbMessage toEwbMessage(SignEwbRequestDto dto);
}
