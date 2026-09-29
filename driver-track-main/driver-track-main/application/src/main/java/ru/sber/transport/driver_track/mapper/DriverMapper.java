package ru.sber.transport.driver_track.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;

@Mapper
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget DriverMessageRecord target, DriverMessage source);
}
