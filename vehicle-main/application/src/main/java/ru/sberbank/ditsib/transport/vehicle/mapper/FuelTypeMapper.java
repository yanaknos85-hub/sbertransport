package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelTypeName;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeDto;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.FuelTypeMessage;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;

@Mapper(componentModel = "spring", imports = {EngineTypeMapper.class, FuelTypeName.class})
public interface FuelTypeMapper {

    @Mapping(target = "possibleTitles", source = "fuelTypeNames")
    FuelTypeDto fueltTypeToFuelTypeDto(FuelType source);

    List<FuelTypeDto> listFuelTypeToListFuelTypeDto(List<FuelType> source);

    @Mapping(target = "possibleTitles", source = "source.fuelTypeNames")
    @Mapping(target = "engineTypeId", source = "source.engineType.id")
    @Mapping(target = "deleted", source = "deleted")
    FuelTypeMessage fuelTypeToFuelTypeMessage(FuelType source, boolean deleted);

    Set<UUID> fuelTypesToFuelTypeUUIDs(Set<FuelType> source);

    default UUID fuelTypeToFuelTypeUUID(FuelType fuelType) {
        return fuelType != null ? fuelType.getId() : null;
    }
    
    default List<String> fuelTypeNameListToStringList(List<FuelTypeName> source) {
        return source != null ? source.stream().map(FuelTypeName::getName).toList() : emptyList();
    }

}
