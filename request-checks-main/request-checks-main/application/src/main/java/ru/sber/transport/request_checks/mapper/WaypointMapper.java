package ru.sber.transport.request_checks.mapper;

import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request_checks.entity.WaypointEntity;
import ru.sber.transport.request_checks.messaging.message.ExternalRequestMessage;

@Mapper
public interface WaypointMapper {

    @Mapping(target = "tripRequestId", source = "tripRequestId")
    @Mapping(target = "orderingIndex", source = "orderingIndex")
    WaypointEntity waypointMessageToWaypointEntity(ExternalRequestMessage.WaypointMessage waypointMessage,
        Integer orderingIndex, UUID tripRequestId);

    @Mapping(target = "tripRequestId", source = "tripRequestId")
    @Mapping(target = "orderingIndex", source = "orderingIndex")
    @Mapping(target = "latitude", source = "waypoint.address.latitude")
    @Mapping(target = "longitude", source = "waypoint.address.longitude")
    WaypointEntity requestMessageWaypointToWaypointEntity(RequestMessage.Waypoint waypoint,
        Integer orderingIndex, UUID tripRequestId);

}
