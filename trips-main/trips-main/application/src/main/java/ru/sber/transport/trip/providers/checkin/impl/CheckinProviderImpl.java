package ru.sber.transport.trip.providers.checkin.impl;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.model.Checkin;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.database.trips.Keys;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.CheckInRecord;
import ru.sber.transport.trip.providers.checkin.CheckinProvider;
import ru.sber.transport.trip.providers.checkin.mapper.CheckinMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CheckinProviderImpl implements CheckinProvider {

    private final DSLContext dslContext;

    private final CheckinMapper checkinMapper;

    @Override
    public int save(Checkin checkin) {
        var checkInRecord = checkinMapper.toRecord(checkin);
        return dslContext.insertInto(Tables.CHECK_IN).set(checkInRecord)
                .onConflict(Keys.PK_CHECK_IN.getFields()).doUpdate().set(checkInRecord).execute();
    }

    @Override
    public Optional<Checkin> get(UUID id) {
        return dslContext.selectFrom(Tables.CHECK_IN)
                .where(Tables.CHECK_IN.ID.eq(id))
                .fetchOptional().map(checkinMapper::toModel);
    }

    @Override
    public List<Checkin> findAllByTripId(UUID tripId) {
        return dslContext.selectFrom(Tables.CHECK_IN)
                .where(Tables.CHECK_IN.TRIP_ID.eq(tripId))
                .fetchInto(CheckInRecord.class)
                .stream().map(checkinMapper::toModel)
                .collect(Collectors.toList()); //NOSONAR
    }

    @Override
    public List<Checkin> findAllByTripIds(List<UUID> tripIds) {
        return dslContext.selectFrom(Tables.CHECK_IN)
                .where(Tables.CHECK_IN.TRIP_ID.in(tripIds))
                .fetchInto(CheckInRecord.class)
                .stream().map(checkinMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Checkin> findAllByTripIdsAndStatus(List<UUID> tripIds, TripStatus status) {
        return dslContext.selectFrom(Tables.CHECK_IN)
                .where(Tables.CHECK_IN.TRIP_ID.in(tripIds))
                .and(Tables.CHECK_IN.STATUS.eq(status.toString()))
                .fetchInto(CheckInRecord.class)
                .stream().map(checkinMapper::toModel)
                .collect(Collectors.toList());
    }
}
