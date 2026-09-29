package ru.sber.transport.trip.providers.autopark.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.trip.database.trips.tables.records.AutoparkRecord;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;


@Mapper
public interface AutoparkMapper {

    @Mapping(target = "active", expression = "java(deletedToActive(message.deleted()))")
    AutoparkRecord toRecord(AutoparkMessage message);

    default boolean deletedToActive(boolean deleted) {
        return !deleted;
    }

}
