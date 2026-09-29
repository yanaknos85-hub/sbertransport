package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.notifications.database.model.request.Vehicle;
import ru.sber.transport.request.messaging.TaxiTripMessage;

@Mapper
public interface VehicleMessageMapper {

    @Mapping(target = "registrationNumber", source = "stateNumber")
    Vehicle toModel(TaxiTripMessage.Vehicle source);

}
