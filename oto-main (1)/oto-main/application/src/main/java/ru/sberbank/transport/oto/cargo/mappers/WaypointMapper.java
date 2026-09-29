package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.transport.oto.cargo.database.model.Waypoint;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestMessage;

@Mapper(uses = AddressMapper.class)
public interface WaypointMapper {

    RequestMessage.Waypoint toMessage(Waypoint waypoint);

    Waypoint toModel(RequestMessage.Waypoint waypoint);

}
