package ru.sberbank.ditsib.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.database.model.Position;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

/**
 * Маппер должностей.
 */
@Mapper
public interface PositionMapper {

    @Mapping(source = "organizationId", target = "organization.id")
    Position positionMessageToPosition(PositionMessage source);
}
