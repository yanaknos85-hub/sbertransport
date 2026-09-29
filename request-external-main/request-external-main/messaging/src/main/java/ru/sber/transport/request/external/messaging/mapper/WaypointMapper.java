package ru.sber.transport.request.external.messaging.mapper;

import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import ru.sber.transport.request.external.messaging.message.RequestMessage;
import ru.sber.transport.request.external.model.WaypointData;

@Mapper
public interface WaypointMapper {

    RequestMessage.WaypointMessage toMessage(WaypointData waypointData);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<RequestMessage.WaypointMessage> toMessage(List<WaypointData> waypoints);

    ru.sber.transport.messages.request.external.avro.WaypointMessage waypointToWaypoint(WaypointData waypoint);
}
