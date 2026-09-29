package ru.sber.transport.trips.cargo.messaging.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.trips.cargo.message.TripMessage;
import ru.sber.transport.trips.cargo.business.model.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Маппер поездок.
 */
@Mapper
public interface MessageTripMapper {

    /**
     * Преобразование объекта в сообщение.
     *
     * @param trip исходный объект.
     * @return сообщение.
     */
    @Mapping(target = "contractorDigitId", ignore = true)
    @Mapping(target = "additional", source = "trip")
    @Mapping(target = "requests", source = "trip")
    TripMessage toMessage(Trip trip);

    default List<Map<String, Object>> toRequestMessage(Trip source) {
        var mapFunction = (Function<CargoRequest, Map<String, Object>>) this::toMessage;
        return source.getRequests().stream().map(mapFunction).toList();
    }

    default Map<String, Object> toMessage(CargoRequest request) {
        return objectMapper().convertValue(request, new TypeReference<>() {});
    }

    default List<Map<String, Object>> toMessage(List<Waypoint> waypoint) {
        return objectMapper().convertValue(waypoint, new TypeReference<>() {
        });
    }

    default LocalDateTime toLocalDateTime (OffsetDateTime offsetDateTime){
        if(offsetDateTime!=null){
            return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } else return null;
    }

    default TripMessage.AdditionalData attachAdditionalData(Trip trip) {
        return new TripMessage.AdditionalData(trip.getCapacity());
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }

}
