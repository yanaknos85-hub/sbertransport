package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;
import ru.sber.transport.notifications.database.model.TripPurpose;

/**
 * Маппер целей.
 */
@Mapper
public interface TripPurposeMapper {

    /**
     * Обновить данные.
     *
     * @param target цель.
     * @param source источник.
     */
    @Mapping(target = "organizationId", source = "organization")
    @Mapping(target = "purpose", source = "label")
    void update(@MappingTarget TripPurpose target, TripPurposeMessage source);

}
