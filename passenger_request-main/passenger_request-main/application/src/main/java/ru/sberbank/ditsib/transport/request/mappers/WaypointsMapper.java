package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.*;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;

import java.util.List;

/**
 * Маппер путевых точек.
 */
@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = AddressMapper.class)
public interface WaypointsMapper {
    
    @Mapping(target = "waitTime", source = "waitTime")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "id", source = "id")
    RequestMessage.Waypoint toMessage(Waypoint waypoint);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<RequestMessage.Waypoint> toMessage(List<Waypoint> waypoints);
}
