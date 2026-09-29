package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sberbank.ditsib.transport.vehicle.database.model.Category;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType;
import ru.sberbank.ditsib.transport.vehicle.database.model.Vehicle;
import ru.sberbank.ditsib.transport.vehicle.database.projection.VehicleShortProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleShortDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        imports = {
        ModelMapper.class,
        FuelTypeMapper.class,
        Category.class,
        DriveMapper.class
})
public interface VehicleMapper {
    Vehicle vehicleRequestDtoToVehicle(VehicleRequestDto source);
    
    default String trimmed(String fieldValue) {
        return Optional.ofNullable(fieldValue).map(String::trim).orElse(null);
    }

    @Mapping(target = "dimensions", source = "source", qualifiedByName = "mapDimensions")
    @Mapping(target = "manufacturePeriod", source = "source", qualifiedByName = "mapManufacturePeriod")
    VehicleShortDto vehicleProjectionToVehicleShortDto(VehicleShortProjection source);
    
    List<VehicleShortDto> listVehicleProjectionToListVehicleShortDto(List<VehicleShortProjection> source);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "brand", source = "model.brand.title")
    @Mapping(target = "model", source = "model.title")
    @Mapping(target = "engineType", source = "engineType.title")
    @Mapping(target = "fuelType", source = "source.fuelTypes", qualifiedByName = "mapFuelTypesToString")
    @Mapping(target = "engineCapacity", source = "engineCapacity")
    @Mapping(target = "enginePower", source = "enginePower")
    @Mapping(target = "fuelTankVolume", source = "fuelTankVolume")
    @Mapping(target = "drive", source = "drive.title")
    @Mapping(target = "mudguardInstalled", source = "mudguardInstalled")
    @Mapping(target = "spareWheelHolderInstalled", source = "spareWheelHolderInstalled")
    @Mapping(target = "bodyType", source = "bodyType.title")
    @Mapping(target = "transmissionType", source = "transmissionType.title")
    @Mapping(target = "weight", source = "weight")
    @Mapping(target = "dimensions", source = "source", qualifiedByName = "mapDimensions")
    @Mapping(target = "manufacturePeriod", source = "source", qualifiedByName = "mapManufacturePeriod")
    VehicleShortDto vehicleToVehicleShortDto(Vehicle source);

    @Named("mapDimensions")
    default String mapDimensions(Vehicle source) {
        return "%dx%dx%d".formatted(source.getLength(), source.getWidth(), source.getHeight());
    }

    @Named("mapDimensions")
    default String mapDimensions(VehicleShortProjection source) {
        return "%dx%dx%d".formatted(source.getLength(), source.getWidth(), source.getHeight());
    }

    @Named("mapManufacturePeriod")
    default String mapManufacturePeriod(Vehicle source) {
        return "%s - %s".formatted(source.getYearManufactureBegin(),
                Optional.ofNullable(source.getYearManufactureEnd())
                .map(Object::toString)
                .orElse("н.в."));
    }

    @Named("mapManufacturePeriod")
    default String mapManufacturePeriod(VehicleShortProjection source) {
        return "%s - %s".formatted(source.getYearManufactureBegin(),
                Optional.ofNullable(source.getYearManufactureEnd())
                        .map(Object::toString)
                        .orElse("н.в."));
    }

    List<VehicleShortDto> listVehicleToListVehicleShortDto(List<Vehicle> source);

    @Named("mapFuelTypesToString")
    public static String mapFuelTypesToString(Set<FuelType> fuelTypes) {
        return fuelTypes.stream()
                .map(FuelType::getTitle)
                .collect(Collectors.joining(", "));
    }
}
