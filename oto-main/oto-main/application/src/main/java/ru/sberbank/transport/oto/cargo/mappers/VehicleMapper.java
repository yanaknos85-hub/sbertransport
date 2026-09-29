package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.transport.oto.cargo.database.model.Vehicle;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestMessage;

@Mapper
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Vehicle vehicle, RequestMessage.VehicleData vehicleMessage);
    
    Vehicle vehicle(RequestMessage.VehicleData driverMessage);

}
