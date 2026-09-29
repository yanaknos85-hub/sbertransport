package ru.sber.transport.trip.providers.history.trip.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.trip.business.model.TripHistoryItem;
import ru.sber.transport.trip.database.trips.tables.records.TripHistoryRecord;

@Mapper
public interface TripHistoryMapper {

    TripHistoryRecord toRecord(TripHistoryItem tripHistoryItem);

    TripHistoryItem toModel(TripHistoryRecord record);

}
