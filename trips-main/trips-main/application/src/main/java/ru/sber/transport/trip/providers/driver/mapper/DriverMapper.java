package ru.sber.transport.trip.providers.driver.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.trip.business.dto.DriverOnMapDto;
import ru.sber.transport.trip.business.dto.DriverShiftDTO;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.database.trips.tables.records.DriverRecord;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Mapper
public interface DriverMapper {

    @Mapping(target = "shiftId", ignore = true)
    @Mapping(target = "online", ignore = true)
    @Mapping(target = "serving", ignore = true)
    @Mapping(target = "activeTripId", ignore = true)
    Driver toModel(DriverMessage driverMessage);

    @Mapping(target = "pointTime", qualifiedByName = "offsetToZoned")
    Driver toModel(DriverRecord driverRecord);

    @Mapping(target = "pointTime", qualifiedByName = "zonedToOffset")
    DriverRecord toRecord(Driver driver);

    @Mapping(target = "activeShiftId", source = "driver.shiftId")
    DriverMessage toMessage (Driver driver);

    DriverShiftDTO toDriverShiftDto(Driver driver);

    DriverOnMapDto toDriverOnMapDto(Driver driver);

    @Named("offsetToZoned")
    default ZonedDateTime offsetToZoned(OffsetDateTime offsetDateTime){
        return offsetDateTime != null ? ZonedDateTime.of(offsetDateTime.toLocalDateTime(), offsetDateTime.getOffset()) : null;
    }

    @Named("zonedToOffset")
    default OffsetDateTime zonedToOffset(ZonedDateTime zonedDateTime){
        return zonedDateTime != null ? OffsetDateTime.of(zonedDateTime.toLocalDateTime(), zonedDateTime.getOffset()) : null;
    }

}
