package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sber.transport.telemechanic.database.model.Position;

/**
 * Маппер должностей.
 */
@Mapper(componentModel = "spring")
public interface PositionMapper {

    @Mapping(source = "organizationId", target = "organization.id")
    Position positionMessageToPosition(PositionMessage source);
}
