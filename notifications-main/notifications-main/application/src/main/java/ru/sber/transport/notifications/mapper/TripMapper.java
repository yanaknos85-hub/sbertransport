package ru.sber.transport.notifications.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.notifications.database.model.trip.Trip;
import ru.sber.transport.notifications.messaging.message.TripMessage;

@Mapper
public interface TripMapper {

    @Mapping(target = "humanReadableId", source = "message")
    @Mapping(target = "driver.id", source = "driverId")
    @Mapping(target = "driver.contractorId", source = "contractorId")
    @Mapping(target = "vehicle.id", source = "vehicleId")
    Trip toModel(TripMessage message);

    default String getHumanReadableId(TripMessage message) {
        return "TR-%04d-%08d".formatted(message.getContractorDigitId(), message.getDigitId());
    }

}
