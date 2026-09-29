package ru.sber.transport.trips.cargo.providers.checkin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sber.transport.trips.cargo.business.dto.CheckinDTO;
import ru.sber.transport.trips.cargo.business.dto.v2.CheckinDtoV2;
import ru.sber.transport.trips.cargo.business.model.Checkin;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.CheckInRecord;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Mapper
public interface CheckinMapper {

    @Mapping(target = "time", qualifiedByName = "zonedToOffset")
    CheckInRecord toRecord(Checkin checkin);

    @Mapping(target = "time", qualifiedByName = "offsetToZoned")
    Checkin toModel(CheckInRecord checkInRecord);

    CheckinDTO toDto(Checkin checkin);

    @Mapping(target = "time", expression = "java(mapTime(checkin))")
    CheckinDtoV2 toDtoV2(Checkin checkin);

    @Named("offsetToZoned")
    default ZonedDateTime offsetToZoned(OffsetDateTime offsetDateTime){
        return offsetDateTime != null ? ZonedDateTime.of(offsetDateTime.toLocalDateTime(), offsetDateTime.getOffset()) : null;
    }

    @Named("zonedToOffset")
    default OffsetDateTime zonedToOffset(ZonedDateTime zonedDateTime){
        return zonedDateTime != null ? OffsetDateTime.of(zonedDateTime.toLocalDateTime(), zonedDateTime.getOffset()) : null;
    }

    @Named("mapTime")
    default OffsetDateTime mapTime(Checkin checkin){
        var zone = ZoneId.of(checkin.getTimeZone()).normalized();
        return OffsetDateTime.of(checkin.getTime().withZoneSameInstant(zone).toLocalDateTime(), ZoneOffset.of(zone.getId()));
    }

}
