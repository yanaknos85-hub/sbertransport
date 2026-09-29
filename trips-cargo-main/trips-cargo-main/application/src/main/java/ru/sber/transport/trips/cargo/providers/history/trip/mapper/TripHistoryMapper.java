package ru.sber.transport.trips.cargo.providers.history.trip.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.trips.cargo.business.model.TripHistoryItem;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.TripHistoryRecord;

@Mapper
public interface TripHistoryMapper {

    TripHistoryRecord toRecord(TripHistoryItem tripHistoryItem);

}
