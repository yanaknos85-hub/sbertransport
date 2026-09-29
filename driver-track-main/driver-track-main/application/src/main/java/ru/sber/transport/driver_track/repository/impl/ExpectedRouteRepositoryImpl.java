package ru.sber.transport.driver_track.repository.impl;

import org.springframework.stereotype.Repository;
import ru.sber.transport.driver_track.database.driver_track.Tables;
import ru.sber.transport.driver_track.database.driver_track.tables.ExpectedRoute;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.repository.ExpectedRouteRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ExpectedRouteRepositoryImpl implements ExpectedRouteRepository {

    @Override
    public Optional<ExpectedRouteRecord> findByTripIdAndSource(UUID tripId, RouteSource sourceType) {
        return context().selectFrom(table())
                .where(table().TRIP_ID.eq(tripId))
                .and(table().SOURCE.eq(sourceType.name()))
                .fetchOptionalInto(ExpectedRouteRecord.class);
    }

    @Override
    public void insertIfNotExist(ExpectedRouteRecord expectedRouteRecord) {
        ExpectedRoute table = table();
        context().insertInto(table)
                .set(table.ID, expectedRouteRecord.getId())
                .set(table.TRIP_ID, expectedRouteRecord.getTripId())
                .set(table.SOURCE, expectedRouteRecord.getSource())
                .set(table.COORDS, expectedRouteRecord.getCoords())
                .onConflict(table.TRIP_ID, table.SOURCE)
                .doNothing()
                .execute();
    }

    @Override
    public ExpectedRoute table() { return Tables.EXPECTED_ROUTE; }
}
