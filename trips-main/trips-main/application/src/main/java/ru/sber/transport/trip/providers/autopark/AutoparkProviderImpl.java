package ru.sber.transport.trip.providers.autopark;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.database.trips.Keys;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.AutoparkRecord;
import ru.sber.transport.trip.messaging.providers.AutoparkProvider;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.trip.providers.autopark.mapper.AutoparkMapper;

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
