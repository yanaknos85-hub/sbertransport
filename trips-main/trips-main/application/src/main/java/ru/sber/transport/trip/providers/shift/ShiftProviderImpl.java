package ru.sber.transport.trip.providers.shift;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.model.Shift;
import ru.sber.transport.trip.database.trips.Keys;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.ShiftRecord;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.providers.shift.mapper.ShiftMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
                .fetchInto(ShiftRecord.class)
                .stream().map(shiftMapper::toModel).collect(Collectors.toList()); //NOSONAR
    }

    @Override
    public List<Shift> getShiftByVehicleIdAndCurrentDate(UUID vehicleId, LocalDateTime time) {
        return dslContext.selectFrom(Tables.SHIFT)
                .where(Tables.SHIFT.VEHICLE_ID.eq(vehicleId))
                .and(Tables.SHIFT.START_DATE.lessThan(time))
                .and(Tables.SHIFT.END_DATE.greaterThan(time))
                .and(Tables.SHIFT.DELETED.eq(false))
                .fetchInto(ShiftRecord.class)
                .stream().map(shiftMapper::toModel).collect(Collectors.toList()); //NOSONAR
    }

    @Override
    public List<Shift> getAllByIds(List<UUID> ids) {
        return dslContext.selectFrom(Tables.SHIFT)
                .where(Tables.SHIFT.ID.in(ids))
                .fetchInto(ShiftRecord.class).stream()
                .map(shiftMapper::toModel).toList();
    }

    @Override
    public Optional<Shift> getByEwbId(UUID ewbId) {
        return dslContext.selectFrom(Tables.SHIFT)
                .where(Tables.SHIFT.EWB_ID.eq(ewbId))
                .fetchOptional().map(shiftMapper::toModel);
    }
}