package ru.sber.transport.trips.cargo.providers.integration_client;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trips.cargo.business.model.IntegrationClient;
import ru.sber.transport.trips.cargo.messaging.providers.IntegrationClientProvider;
import ru.sber.transport.trips.cargo.providers.integration_client.mapper.IntegrationClientMapper;
import ru.sber.transport.trips_cargo.database.trips_cargo.Keys;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;


import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IntegrationClientProviderImpl implements IntegrationClientProvider {

    private final DSLContext dslContext;

    private final IntegrationClientMapper mapper;

    @Override
    public void save(IntegrationClient client) {
        var clientRecord = mapper.toRecord(client);
        dslContext.insertInto(Tables.INTEGRATION_CLIENT).set(clientRecord)
                .onConflict(Keys.PK_INTEGRATION_CLIENT.getFields())
                .doUpdate().set(clientRecord).execute();
    }

    @Override
    public Optional<IntegrationClient> get(UUID id) {
        return dslContext.selectFrom(Tables.INTEGRATION_CLIENT)
                .where(Tables.INTEGRATION_CLIENT.ID.eq(id))
                .fetchOptional().map(mapper::toModel);
    }
}
