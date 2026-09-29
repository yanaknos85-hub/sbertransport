package ru.sber.transport.trips.cargo.providers.autopark;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.trips.cargo.messaging.providers.AutoparkProvider;
import ru.sber.transport.trips.cargo.providers.autopark.mapper.AutoparkMapper;
import ru.sber.transport.trips_cargo.database.trips_cargo.Keys;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.AutoparkRecord;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AutoparkProviderImpl implements AutoparkProvider {

    private final DSLContext dslContext;

    private final AutoparkMapper autoparkMapper;

    @Override
    public int save(AutoparkMessage autoparkMessage) {
        var autopark = autoparkMapper.toRecord(autoparkMessage);
        return dslContext.insertInto(Tables.AUTOPARK).set(autopark)
                .onConflict(Keys.PK_AUTOPARK.getFields()).doUpdate().set(autopark).execute();
    }

    @Override
    public Optional<AutoparkRecord> getByRoutingId(UUID routingId) {
        return dslContext.selectFrom(Tables.AUTOPARK).where(Tables.AUTOPARK.ROUTING_ID.eq(routingId)).fetchOptional();
    }
}
