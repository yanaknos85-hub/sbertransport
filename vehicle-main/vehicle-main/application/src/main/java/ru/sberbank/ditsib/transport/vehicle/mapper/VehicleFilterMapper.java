package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.VehicleSearchFiltersProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleSearchFilters;

@Mapper(componentModel = "spring")
public interface VehicleFilterMapper {


    VehicleSearchFilters vehicleSearchFiltersProjectionToVehicleSearchFilters(VehicleSearchFiltersProjection filters);

}
