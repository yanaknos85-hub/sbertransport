package ru.sber.transport.trip.providers.column_settings.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.database.trips.Keys;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.ColumnSettings;
import ru.sber.transport.trip.database.trips.tables.records.ColumnSettingsRecord;
import ru.sber.transport.trip.providers.column_settings.ColumnSettingsProvider;
import ru.sber.transport.trip.providers.column_settings.mapping.ColumnSettingsMapper;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class ColumnSettingsProviderImpl implements ColumnSettingsProvider {

    private final DSLContext dslContext;

    private final ColumnSettingsMapper columnSettingsMapper;

    @Override
    public Map<String, Object> save(UUID userId, Map<String, Object> setting) {
        var settings = new ColumnSettingsRecord(userId, columnSettingsMapper.toJson(setting));
        dslContext.insertInto(Tables.COLUMN_SETTINGS).set(settings)
                .onConflict(Keys.PK_COLUMN_SETTINGS.getFields()).doUpdate().set(settings).execute();
        return get(userId);
    }

    @Override
    public void update(UUID userId, Map<String, Object> setting) {
        var settings = new ColumnSettingsRecord(userId, columnSettingsMapper.toJson(setting));
        dslContext.update(Tables.COLUMN_SETTINGS).set(settings).execute();
    }

    @Override
    public void delete(UUID userId) {
        dslContext.deleteFrom(Tables.COLUMN_SETTINGS).where(Tables.COLUMN_SETTINGS.USER_ID.eq(userId)).execute();
    }

    @Override
    public Map<String, Object> get(UUID userId) {
        var result = dslContext.select(Tables.COLUMN_SETTINGS.SETTING).from(Tables.COLUMN_SETTINGS)
                .where(Tables.COLUMN_SETTINGS.USER_ID.eq(userId))
                .fetchOptional();
        if (result.isPresent()) {
            return columnSettingsMapper.toMap(result.get().value1());
        } else throw new EntityNotFoundException(ColumnSettingsRecord.class, userId, false);
    }
}
