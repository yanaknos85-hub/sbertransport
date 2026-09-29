package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Position;

/**
 * Маппер должностей.
 */
@Mapper(componentModel = "spring")
public interface PositionMapper {
    
    @Mapping(source = "organizationId", target = "organization.id")
    Position positionMessageToPosition(PositionMessage source);
}
