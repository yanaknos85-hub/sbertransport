package ru.sber.transport.driver_track.repository.impl;

import org.springframework.stereotype.Repository;
import ru.sber.transport.driver_track.database.driver_track.Tables;
import ru.sber.transport.driver_track.database.driver_track.tables.Route;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.repository.RouteRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class RouteRepositoryImpl implements RouteRepository {

    @Override
    public Optional<RouteRecord> findByTripIdAndSource(UUID tripId, RouteSource sourceType) {
        return context().selectFrom(table())
                .where(table().TRIP_ID.eq(tripId))
                .and(table().SOURCE.eq(sourceType.name()))
                .fetchOptionalInto(RouteRecord.class);
    }

    @Override
    public void insertIfNotExist(RouteRecord routeRecord) {
        Route table = table();
        context().insertInto(table)
                .set(table.ID, routeRecord.getId())
                .set(table.TRIP_ID, routeRecord.getTripId())
                .set(table.SOURCE, routeRecord.getSource())
                .set(table.COORDS, routeRecord.getCoords())
                .onConflict(table.TRIP_ID, table.SOURCE)
                .doNothing()
                .execute();
    }

    @Override
    public Route table() {
        return Tables.ROUTE;
    }
}
