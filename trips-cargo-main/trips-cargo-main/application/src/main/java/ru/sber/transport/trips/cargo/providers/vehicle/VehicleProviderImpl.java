package ru.sber.transport.trips.cargo.providers.vehicle;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trips.cargo.business.model.Vehicle;
import ru.sber.transport.trips.cargo.messaging.providers.VehicleProvider;
import ru.sber.transport.trips.cargo.providers.vehicle.mapper.VehicleMapper;
import ru.sber.transport.trips_cargo.database.trips_cargo.Keys;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

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
}
