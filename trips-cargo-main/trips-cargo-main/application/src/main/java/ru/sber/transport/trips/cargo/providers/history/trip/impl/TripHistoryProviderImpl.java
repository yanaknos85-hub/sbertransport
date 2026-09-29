package ru.sber.transport.trips.cargo.providers.history.trip.impl;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trips.cargo.business.model.TripHistoryItem;
import ru.sber.transport.trips.cargo.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trips.cargo.providers.history.trip.mapper.TripHistoryMapper;
import ru.sber.transport.trips_cargo.database.trips_cargo.Keys;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;

import java.util.concurrent.CompletionStage;

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
}
