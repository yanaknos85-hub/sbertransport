package ru.sber.transport.request.external.providers.position;

import static ru.sber.transport.database.external_request.Tables.POSITION_TRANSPORT_TYPE;
import static ru.sber.transport.database.external_request.Tables.TRANSPORT_TYPES;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.PositionsProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.Position;
import ru.sber.transport.database.external_request.tables.records.PositionRecord;

@Transactional
@RequiredArgsConstructor
public class PositionsDataProviderImpl implements PositionsProvider, JooqRepository<Position, PositionRecord, UUID> {

    @Override
    public Position table() {
        return Tables.POSITION;
    }

    @Override
    public void save(ru.sber.transport.request.external.model.Position source) {
        for (final var clazz : source.getAvailableClasses()) {
            context().insertInto(TRANSPORT_TYPES)
                    .values(clazz)
                    .onConflictDoNothing()
                    .execute();
        }
        context().insertInto(table())
                .set(table().ID, source.getId())
                .onConflict().doNothing().execute();
        context().deleteFrom(POSITION_TRANSPORT_TYPE).where(POSITION_TRANSPORT_TYPE.POSITION_ID.eq(source.getId())).execute();
        for (final var clazz : source.getAvailableClasses()) {
            context().insertInto(POSITION_TRANSPORT_TYPE)
                    .set(POSITION_TRANSPORT_TYPE.POSITION_ID, source.getId())
                    .set(POSITION_TRANSPORT_TYPE.TRANSPORT_TYPE, clazz)
                    .onConflictDoNothing().execute();
        }
    }
}
