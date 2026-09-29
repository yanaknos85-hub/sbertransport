package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.dispatcher.database.model.Trip;
import ru.sber.transport.trip.message.TripMessage;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper
public interface TripsMapper {

    @Mapping(target = "startTime", source = "expectedStartTime")
    @Mapping(target = "endTime", source = "expectedEndTime")
    Trip toModel(TripMessage tripMessage);

    default OffsetDateTime toOffset(LocalDateTime localDateTime){
        if(localDateTime!=null){
            return OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
        } else return null;
    }

}
