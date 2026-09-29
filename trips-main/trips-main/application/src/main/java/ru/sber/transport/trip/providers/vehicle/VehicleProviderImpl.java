package ru.sber.transport.trip.providers.vehicle;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.model.Vehicle;
import ru.sber.transport.trip.database.trips.Keys;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.VehicleRecord;
import ru.sber.transport.trip.messaging.providers.VehicleProvider;
import ru.sber.transport.trip.providers.vehicle.mapper.VehicleMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VehicleProviderImpl implements VehicleProvider {

    private final DSLContext dslContext;

    private final VehicleMapper vehicleMapper;

    @Override
    public int save(Vehicle vehicle) {
        var vehicleRecord = vehicleMapper.toRecord(vehicle);
        return dslContext.insertInto(Tables.VEHICLE).set(vehicleRecord)
                .onConflict(Keys.PK_VEHICLE.getFields()).doUpdate().set(vehicleRecord).execute();
    }

    @Override
    public Optional<Vehicle> get(UUID id) {
        return dslContext.selectFrom(Tables.VEHICLE)
                .where(Tables.VEHICLE.ID.eq(id))
                .fetchOptional().map(vehicleMapper::toModel);
    }

    @Override
    public Optional<Vehicle> getByIdAndContractorId(UUID id, UUID contractorId) {
        return dslContext.selectFrom(Tables.VEHICLE)
                .where(Tables.VEHICLE.ID.eq(id))
                .and(Tables.VEHICLE.CONTRACTOR_ID.eq(contractorId))
                .fetchOptional().map(vehicleMapper::toModel);
    }

    @Override
    public List<Vehicle> getAllByIds(List<UUID> ids) {
        return dslContext.selectFrom(Tables.VEHICLE)
                .where(Tables.VEHICLE.ID.in(ids))
                .fetchInto(VehicleRecord.class)
                .stream().map(vehicleMapper::toModel)
                .toList();
    }
}
