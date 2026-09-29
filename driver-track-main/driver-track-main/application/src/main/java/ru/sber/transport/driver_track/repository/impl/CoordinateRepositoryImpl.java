package ru.sber.transport.driver_track.repository.impl;

import org.springframework.stereotype.Repository;
import ru.sber.transport.driver_track.database.driver_track.Tables;
import ru.sber.transport.driver_track.database.driver_track.tables.Coordinate;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.repository.CoordinateRepository;

import java.util.List;
import java.util.UUID;

@Repository
public class CoordinateRepositoryImpl implements CoordinateRepository {

    @Override
    public List<CoordinateRecord> findCoordinatesByTripId(UUID tripId) {
        return context().selectFrom(table())
                .where(table().TRIP_ID.eq(tripId))
                .orderBy(table().TIME)
                .fetchInto(CoordinateRecord.class);
    }

    @Override
    public void deleteAllByTripId(UUID tripId) {
        context().delete(table()).where(table().TRIP_ID.eq(tripId)).execute();
    }

    @Override
    public void deleteAllByTripIdList(List<UUID> tripIds) {
        context().delete(table()).where(table().TRIP_ID.in(tripIds)).execute();
    }

    @Override
    public Coordinate table() {
        return Tables.COORDINATE;
    }
}
