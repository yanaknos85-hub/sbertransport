package ru.sber.transport.trip.messaging.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.trip.business.model.Waypoint;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Маппер путевых точек.
 */
@Mapper
public interface WaypointMapper {

    /**
     * Преобразование сообщения в модель.
     *
     * @param waypoint сообщение.
     * @return модель.
     */
    default Waypoint toModel(RequestMessage.Waypoint waypoint) {
        var address = waypoint.address();
        var latitude = Optional.ofNullable(address.getLatitude()).map(Double.class::cast).orElseThrow();
        var longitude = Optional.ofNullable(address.getLongitude()).map(Double.class::cast).orElseThrow();
        var country = Optional.ofNullable(address.getCountry()).map(String.class::cast).orElseThrow();
        var region = Optional.ofNullable(address.getRegion()).map(String.class::cast).orElse(null);
        var city = Optional.ofNullable(address.getCity()).map(String.class::cast).orElse(null);
        var street = Optional.ofNullable(address.getStreet()).map(String.class::cast).orElse(null);
        var house = Optional.ofNullable(address.getHouse()).map(String.class::cast).orElse(null);
        var building = Optional.ofNullable(address.getBuilding()).map(String.class::cast).orElse(null);
        var waitingTime = waypoint.waitTime();
        return new Waypoint(waypoint.id(), latitude, longitude, waypoint.orderingIndex(), country, region, city,
                street, house, building, waitingTime, null, null, null);
    }

    /**
     * Преобразование модели.
     *
     * @param waypoint модель.
     * @return сообщение.
     */
    @Mapping(target = "address", source = "waypoint")
    default List<Map<String, Object>> toMessage(List<Waypoint> waypoint) {
        return objectMapper().convertValue(waypoint, new TypeReference<>() {
        });
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }

}
