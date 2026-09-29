package ru.sber.transport.notifications.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.notifications.database.model.request.Vehicle;

@Mapper
public interface VehicleMapper {

    void update(@MappingTarget Vehicle vehicle, ContractorUpdateTripMessage.Vehicle message);
}
