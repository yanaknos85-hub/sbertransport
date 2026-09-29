package ru.sber.transport.driver_track.repository.impl;

import org.springframework.stereotype.Repository;
import ru.sber.transport.driver_track.database.driver_track.Tables;
import ru.sber.transport.driver_track.database.driver_track.tables.DriverMessage;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;
import ru.sber.transport.driver_track.repository.DriverRepository;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.UUID;

@Repository
public class DriverRepositoryImpl implements DriverRepository {

    @Override
    public DriverMessageRecord getByIdNotNull(UUID id) {
        return context().selectFrom(table())
                .where(table().ID.eq(id))
                .and(table().ACTIVE.isTrue())
                .fetchOptional()
                .orElseThrow(() -> new EntityNotFoundException(DriverMessageRecord.class, id));
    }

    @Override
    public DriverMessage table() {
        return Tables.DRIVER_MESSAGE;
    }
}
