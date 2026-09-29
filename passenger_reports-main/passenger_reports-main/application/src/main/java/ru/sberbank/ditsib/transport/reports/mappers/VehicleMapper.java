package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.dto.VehicleDTO;
import ru.sberbank.ditsib.transport.reports.model.Vehicle;

@Mapper
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Vehicle vehicle, RequestMessage.VehicleData vehicleMessage);

    VehicleDTO toDTO(Vehicle vehicle);

    Vehicle vehicle(RequestMessage.VehicleData vehicleData);
}
