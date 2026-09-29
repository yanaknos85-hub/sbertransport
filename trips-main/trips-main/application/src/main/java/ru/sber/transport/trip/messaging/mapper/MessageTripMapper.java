package ru.sber.transport.trip.messaging.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.message.TripMessage;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Маппер поездок.
 */
@Mapper(uses = { RequestMapper.class, WaypointMapper.class })
public interface MessageTripMapper {

    /**
     * Преобразование объекта в сообщение.
     *
     * @param trip исходный объект.
     * @return сообщение.
     */
    @Mapping(target = "contractorDigitId", ignore = true)
    @Mapping(target = "plannedShiftId", ignore = true)
    @Mapping(target = "additional", source = "trip")
    @Mapping(target = "requests", source = "trip")
    @Mapping(target = "vehicleId", ignore = true)
    TripMessage toMessage(Trip trip);

    default List<Map<String, Object>> toRequestMessage(Trip source) {
        var mapFunction = (Function<Request, Map<String, Object>>) this::toMessage;
        return source.getRequests().stream().map(mapFunction).toList();
    }

    default Map<String, Object> toMessage(Request request) {
        return objectMapper().convertValue(request, new TypeReference<>() {});
    }

    default TripMessage.AdditionalData attachAdditionalData(Trip trip) {
        return new TripMessage.AdditionalData(trip.getPassengerCount(), trip.getTaxiClass() != null ? trip.getTaxiClass() : null, trip.getDriverWaitingTime(), null);
    }

    default LocalDateTime toLocalDateTime (OffsetDateTime offsetDateTime){
        if(offsetDateTime!=null){
            return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } else return null;
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }

}
