package ru.sber.transport.request_checks.mapper;

import static ru.sber.transport.request_checks.util.Constants.METERS_IN_KILOMETER;
import static ru.sber.transport.request_checks.util.Constants.YANDEX_TRANSPORT_TYPE;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request_checks.entity.TripRequestEntity;
import ru.sber.transport.request_checks.messaging.message.ExternalRequestMessage;

@Mapper(imports = ZoneOffset.class)
public interface TripRequestMapper {

    @Mapping(target = "transportType", constant = YANDEX_TRANSPORT_TYPE)
    @Mapping(target = "desiredDate", expression = "java(message.getDate().atOffset(ZoneOffset.UTC))")
    @Mapping(target = "distance", expression = "java(getDistance(message))")
    @Mapping(target = "duration", expression = "java(getDuration(message))")
    TripRequestEntity externalRequestMessageToTripRequestEntity(ExternalRequestMessage message);

    @Mapping(target = "desiredDate", expression = "java(message.getDesiredDate().atOffset(ZoneOffset.UTC))")
    @Mapping(target = "distance", expression = "java(getDistance(message))")
    @Mapping(target = "duration", expression = "java(getDuration(message))")
    TripRequestEntity requestMessageToTripRequestEntity(RequestMessage message);

    default Integer getDistance(ExternalRequestMessage message) {
        return Optional.ofNullable(message.getPlanned())
            .map(data -> (int) data.getDistance())
            .orElse(null);
    }

    default Long getDuration(ExternalRequestMessage message) {
        return Optional.ofNullable(message.getPlanned())
            .map(ExternalRequestMessage.PlannedData::getDuration)
            .map(Duration::parse)
            .map(Duration::toSeconds)
            .orElse(null);
    }

    default Integer getDistance(RequestMessage message) {
        return Optional.ofNullable(message.getExpected())
            .map(RequestMessage.ExpectedData::getDistance)
            .map(distance -> (int) Math.round(distance * METERS_IN_KILOMETER))
            .orElse(null);
    }

    default Long getDuration(RequestMessage message) {
        return Optional.ofNullable(message.getExpected())
            .map(RequestMessage.ExpectedData::getTime)
            .map(Duration::toSeconds)
            .orElse(null);
    }

}
