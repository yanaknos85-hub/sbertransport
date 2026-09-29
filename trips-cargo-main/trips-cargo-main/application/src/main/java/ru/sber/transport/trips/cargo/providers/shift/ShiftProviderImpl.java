package ru.sber.transport.trips.cargo.providers.shift;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trips.cargo.business.model.Shift;
import ru.sber.transport.trips.cargo.messaging.providers.ShiftProvider;
import ru.sber.transport.trips.cargo.providers.shift.mapper.ShiftMapper;
import ru.sber.transport.trips_cargo.database.trips_cargo.Keys;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.ShiftRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ShiftProviderImpl implements ShiftProvider {

    private final DSLContext dslContext;

    private final ShiftMapper shiftMapper;

    @Override
    public int save(Shift shift) {
        var shiftRecord = shiftMapper.toRecord(shift);
        return dslContext.insertInto(Tables.SHIFT).set(shiftRecord)
                .onConflict(Keys.PK_SHIFT.getFields()).doUpdate().set(shiftRecord).execute();
    }

    @Override
    public Optional<Shift> get(UUID id) {
        return dslContext.selectFrom(Tables.SHIFT)
                .where(Tables.SHIFT.ID.eq(id))
                .fetchOptional().map(shiftMapper::toModel);
    }

    @Override
    public List<Shift> getShiftByDriverIdAndCurrentDate(UUID driverId, LocalDateTime time) {
        return dslContext.selectFrom(Tables.SHIFT)
                .where(Tables.SHIFT.DRIVER_ID.eq(driverId))
                .and(Tables.SHIFT.START_DATE.lessThan(time))
                .and(Tables.SHIFT.END_DATE.greaterThan(time))
                .and(Tables.SHIFT.DELETED.eq(false))
                .fetchInto(ShiftRecord.class).stream().map(shiftMapper::toModel).collect(Collectors.toList()); //NOSONAR
    }

    @Override
    public Optional<Shift> getByEwbId(UUID ewbId) {
        return dslContext.selectFrom(Tables.SHIFT)
                .where(Tables.SHIFT.EWB_ID.eq(ewbId))
                .fetchOptional().map(shiftMapper::toModel);
    }
}
