package ru.sber.transport.trips.cargo.providers.autopark.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.AutoparkRecord;


@Mapper
public interface AutoparkMapper {

    @Mapping(target = "active", expression = "java(deletedToActive(message.deleted()))")
    AutoparkRecord toRecord(AutoparkMessage message);

    default boolean deletedToActive(boolean deleted) {
        return !deleted;
    }

}
