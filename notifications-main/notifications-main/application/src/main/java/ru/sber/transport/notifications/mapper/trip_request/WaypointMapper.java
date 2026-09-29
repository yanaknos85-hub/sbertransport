package ru.sber.transport.notifications.mapper.trip_request;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.notifications.database.model.request.Waypoint;

import java.util.List;

/**
 * Маппер путевых точек.
 */
@Mapper(uses = AddressMapper.class)
public interface WaypointMapper {
    
    @Mapping(source = "source.checkinAutomatic", target = "checkinAutomatic")
    @Mapping(source = "source.checkinManual", target = "checkinManual")
    Waypoint toEntity(RequestMessage.Waypoint source);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<Waypoint> toEntity(List<RequestMessage.Waypoint> source);
    
}
