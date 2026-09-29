package ru.sber.transport.dispatcher.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;
import ru.sber.transport.dispatcher.dto.files.vehicle.CargoVehicleFile;
import ru.sber.transport.dispatcher.dto.files.vehicle.PassengerVehicleFile;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.dispatcher.messages.TransportMessage;
import ru.sber.transport.dispatcher.messages.VehicleMessage;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static ru.sber.transport.dispatcher.dto.enums.VehicleType.CARGO;
import static ru.sber.transport.dispatcher.dto.enums.VehicleType.PASSENGER;

@Mapper(uses = {CarModelMapper.class, AutoparkMapper.class, ShiftMapper.class})
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "autopark", ignore = true)
    @Mapping(target = "vehicleAdditional", source = "vehicleAdditional", qualifiedByName = "mapAdditionalToModel")
    void update(@MappingTarget Vehicle vehicle, NewVehicleDTO vehicleDTO);

    @Mapping(target = "vehicleAdditional", expression = "java(mapAdditionalToDto(model))")
    VehicleDTO toDto(Vehicle model, UUID ewbId);

    @Mapping(target = "brand", source = "vehicle.model.brand")
    @Mapping(target = "model", source = "vehicle.model.name")
    @Mapping(target = "trips", ignore = true)
    TransportDTO toTransportDto(Vehicle vehicle);

    @Mapping(target = "brand", source = "model.brand")
    @Mapping(target = "model", source = "model.name")
    ContractorUpdateTripMessage.Vehicle toContractorUpdateTripMessage(Vehicle vehicle);

    @Mapping(target = "brand", source = "model.brand")
    @Mapping(target = "model", source = "model.name")
    @Mapping(target = "contractorId", source = "vehicle.autopark.contractor.id")
    @Mapping(target = "autoparkId", source = "vehicle.autopark.id")
    @Mapping(target = "deleted", expression = "java(!vehicle.isActive())")
    VehicleMessage toVehicleMessage(Vehicle vehicle);

    @Mapping(target = "vehicle", source = "vehicle")
    @Mapping(target = "shifts", expression = "java(fillShifts(vehicle.getId(), shifts))")
    VehicleShiftResponse toVehicleShiftResponse(Vehicle vehicle, List<Shift> shifts);

    @Mapping(target = "width", ignore = true)
    @Mapping(target = "volume", ignore = true)
    @Mapping(target = "length", ignore = true)
    @Mapping(target = "height", ignore = true)
    @Mapping(target = "vehicleType", source = "vehicleType.category")
    @Mapping(target = "autoParkName", source = "autopark.name")
    @Mapping(target = "modelName", source = "model.name")
    @Mapping(target = "modelBrand", source = "model.brand")
    @Mapping(target = "inExploitation", source = "inExploitation", qualifiedByName = "mapInExploitation")
    CargoVehicleFile toCargoVehicleFile(VehicleDTO vehicle);

    @Mapping(target = "modelYear", source = "model.year")
    @Mapping(target = "vehicleType", source = "vehicleType.category")
    @Mapping(target = "autoParkName", source = "autopark.name")
    @Mapping(target = "modelName", source = "model.name")
    @Mapping(target = "modelBrand", source = "model.brand")
    @Mapping(target = "ecoClass", source = "ecoClass.rusName")
    @Mapping(target = "inExploitation", source = "inExploitation", qualifiedByName = "mapInExploitation")
    PassengerVehicleFile toPassengerVehicleFile(VehicleDTO vehicle);

    @Mapping(target = "autopark", ignore = true)
    @Mapping(target = "vehicleType", source = "vehicleType", qualifiedByName = "mapVehicleType")
    @Mapping(target = "model.brand", source = "modelBrand")
    @Mapping(target = "model.name", source = "modelName")
    @Mapping(target = "inExploitation", source = "inExploitation", qualifiedByName = "mapInExploitation")
    NewVehicleDTO toVehicleDto(CargoVehicleFile vehicleFile);

    @Mapping(target = "autopark", ignore = true)
    @Mapping(target = "vehicleType", source = "vehicleType", qualifiedByName = "mapVehicleType")
    @Mapping(target = "model.brand", source = "modelBrand")
    @Mapping(target = "model.name", source = "modelName")
    @Mapping(target = "inExploitation", source = "inExploitation", qualifiedByName = "mapInExploitation")
    @Mapping(target = "ecoClass", source = "ecoClass", qualifiedByName = "mapEcoClass")
    NewVehicleDTO toVehicleDto(PassengerVehicleFile vehicleFile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autopark", ignore = true)
    @Mapping(target = "transportId", source = "message.id")
    @Mapping(target = "mileage", source = "message.currentMileage")
    @Mapping(target = "vehicleType", expression = "java(VehicleType.UNIVERSAL)")
    @Mapping(target = "inExploitation", expression = "java(inExploitation(message))")
    @Mapping(target = "active", expression = "java(isActive(message))")
    @Mapping(target = "model.brand", source = "message.brand")
    @Mapping(target = "model.name", source = "message.model")
    @Mapping(target = "model.year", source = "message.year")
    void updateVehicleEntityWithUniversalType(@MappingTarget Vehicle entity, TransportMessage message);

    @Named("mapEcoClass")
    default EcoClass mapEcoClass(String ecoClass) {
        return Optional.ofNullable(ecoClass)
                .map(EcoClass::fromRusName)
                .orElse(null);
    }

    @Named("mapInExploitation")
    default String mapInExploitation(Boolean inExploitation) {
        return isTrue(inExploitation)
                ? "Да"
                : "Нет";
    }

    @Named("mapInExploitation")
    default Boolean mapInExploitation(String inExploitation) {
        return switch (inExploitation.toLowerCase()) {
            case "да" -> true;
            case "нет" -> false;
            default -> throw new IllegalArgumentException("Unknown value for inExploitation: " + inExploitation);
        };
    }

    @AfterMapping
    default void mapCargoAdditional(@MappingTarget CargoVehicleFile cargoVehicleFile, VehicleDTO vehicle) {
        if (vehicle.getVehicleAdditional() instanceof CargoVehicleData cargoVehicleData) {
            cargoVehicleFile.setVolume(cargoVehicleData.getVolume());
            cargoVehicleFile.setHeight(cargoVehicleData.getHeight());
            cargoVehicleFile.setWidth(cargoVehicleData.getWidth());
            cargoVehicleFile.setLength(cargoVehicleData.getLength());
        }
    }

    @AfterMapping
    default void mapCargoAdditional(@MappingTarget NewVehicleDTO newVehicleDTO, CargoVehicleFile file) {
        if (file.getVolume() == null && file.getHeight() == null && file.getWidth() == null && file.getLength() == null) {
            return;
        }

        newVehicleDTO.setVehicleAdditional(new CargoVehicleData(
                null, Optional.ofNullable(file.getVolume()).orElse(0.0),
                Optional.ofNullable(file.getLength()).orElse(0.0),
                Optional.ofNullable(file.getWidth()).orElse(0.0),
                Optional.ofNullable(file.getLength()).orElse(0.0))
        );
    }

    @Named("mapVehicleType")
    default VehicleType mapVehicleType(String type) {
        return VehicleType.fromCategory(type);
    }

    @Named("mapAdditionalToModel")
    default Map<String, Object> mapAdditional(VehicleAdditional vehicleAdditional) {
        var objectMapper = new ObjectMapper();
        return objectMapper.convertValue(vehicleAdditional, new TypeReference<>() {
        });
    }

    default VehicleAdditional mapAdditionalToDto(Vehicle model) {
        var objectMapper = new ObjectMapper();
        if (model.getVehicleType().equals(PASSENGER)) {
            return objectMapper.convertValue(model.getVehicleAdditional(), TaxiVehicleData.class);
        } else if (model.getVehicleType().equals(CARGO)) {
            return objectMapper.convertValue(model.getVehicleAdditional(), CargoVehicleData.class);
        } else {
            return null;
        }
    }

    default List<VehicleShiftResponse.Shift> fillShifts(UUID vehicleId, List<Shift> shifts) {
        if (shifts == null || shifts.isEmpty()) {
            return Collections.emptyList();
        }
        var vehicleShifts = shifts.parallelStream().filter(shift -> shift.getVehicle().getId().equals(vehicleId)).collect(Collectors.toList()); //NOSONAR
        if (vehicleShifts.isEmpty()) {
            return Collections.emptyList();
        } else {
            return vehicleShifts.parallelStream().map(shift -> {
                var shiftResponse = new VehicleShiftResponse.Shift();
                shiftResponse.setId(shift.getId());
                shiftResponse.setStartDate(shift.getStartDate());
                shiftResponse.setEndDate(shift.getEndDate());
                shiftResponse.setActive(shift.isActive());
                shiftResponse.setRowId(shift.getRowId());
                shiftResponse.setDriver(getDriver(shift));
                shiftResponse.setEwbId(shift.getEwbId());
                return shiftResponse;
            }).toList();
        }
    }

    private static VehicleShiftResponse.Shift.Driver getDriver(Shift shift) {
        var vehicleShiftDriver = new VehicleShiftResponse.Shift.Driver();
        vehicleShiftDriver.setId(shift.getDriver().getId());
        vehicleShiftDriver.setFirstName(shift.getDriver().getFirstName());
        vehicleShiftDriver.setLastName(shift.getDriver().getLastName());
        vehicleShiftDriver.setPatronymic(shift.getDriver().getPatronymic());
        vehicleShiftDriver.setHumanReadableId(shift.getDriver().getHumanReadableId());
        vehicleShiftDriver.setDriverSpeciality(shift.getDriver().getDriverSpeciality());
        return vehicleShiftDriver;
    }

    default boolean isActive(TransportMessage message) {
        return Optional.ofNullable(message.status()).map("IN_USE"::equals).orElse(false);
    }

    default boolean inExploitation(TransportMessage message) {
        return message.exploitationStart() != null && message.exploitationEnd() == null;
    }

}
