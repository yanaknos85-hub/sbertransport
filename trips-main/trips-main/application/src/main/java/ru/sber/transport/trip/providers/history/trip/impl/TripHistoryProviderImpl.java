package ru.sber.transport.trip.providers.history.trip.impl;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.model.TripHistoryItem;
import ru.sber.transport.trip.database.trips.Keys;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.TripHistoryRecord;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trip.providers.history.trip.mapper.TripHistoryMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TripHistoryProviderImpl implements TripHistoryProvider {

    private final DSLContext dslContext;

    private final TripHistoryMapper tripHistoryMapper;

    @Override
    public int save(TripHistoryItem tripHistoryItem) {
        var tripHistoryRecord = tripHistoryMapper.toRecord(tripHistoryItem);
        return dslContext.insertInto(Tables.TRIP_HISTORY).set(tripHistoryRecord)
                .onConflict(Keys.TRIP_HISTORY_ITEM_PK.getFields()).doUpdate().set(tripHistoryRecord).execute();
    }

    @Override
    public List<TripHistoryItem> getHistoryByTripId(UUID tripId) {
        return dslContext.selectFrom(Tables.TRIP_HISTORY)
                .where(Tables.TRIP_HISTORY.TRIP_ID.eq(tripId))
                .orderBy(Tables.TRIP_HISTORY.CHANGE_TIME.desc())
                .fetchInto(TripHistoryRecord.class)
                .stream().map(tripHistoryMapper::toModel).collect(Collectors.toList());
    }

    @Override
    public List<TripHistoryItem> getHistoryByTripIds(List<UUID> tripIds) {
        return dslContext.selectFrom(Tables.TRIP_HISTORY)
                .where(Tables.TRIP_HISTORY.TRIP_ID.in(tripIds))
                .fetchInto(TripHistoryRecord.class)
                .stream().map(tripHistoryMapper::toModel).toList();
    }
}
