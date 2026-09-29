package ru.sber.transport.trip.messaging.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.business.model.TripStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Маппер заявок.
 */
@Mapper(uses = {WaypointMapper.class}, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface RequestMapper {

    /**
     * Преобразование сообщения в модель.
     *
     * @param message собщение.
     * @return модель.
     */
    @Mapping(target = "taxiClass", source = "tripClass")
    @Mapping(target = "suburb", source = "suburbTrip")
    @Mapping(target = "comment", source = "commentForDriver")
    Request toModel(RequestMessage message);

    /**
     * Преобразование модели в сообщение.
     *
     * @param status модель.
     * @return сообщение.
     */
    default String toMessage(TripStatus status) {
        return Optional.ofNullable(status).map(TripStatus::name).orElse(null);
    }

    default OffsetDateTime toOffset(LocalDateTime localDateTime){
        if(localDateTime!=null){
            return OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
        } else return null;
    }
}
