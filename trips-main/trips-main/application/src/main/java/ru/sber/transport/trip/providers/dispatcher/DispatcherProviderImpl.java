package ru.sber.transport.trip.providers.dispatcher;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.model.Dispatcher;
import ru.sber.transport.trip.database.trips.Keys;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.DispatcherRecord;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.providers.dispatcher.mapper.DispatcherMapper;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DispatcherProviderImpl implements DispatcherProvider {

    private final DispatcherMapper dispatcherMapper;

    private final DSLContext dslContext;

    @Override
    public int save(Dispatcher dispatcher) {
        var dispatcherRecord = dispatcherMapper.toRecord(dispatcher);
        return dslContext.insertInto(Tables.DISPATCHER).set(dispatcherRecord)
                .onConflict(Keys.PK_DISPATCHER.getFields()).doUpdate().set(dispatcherRecord).execute();
    }

    @Override
    public Optional<Dispatcher> get(UUID contractorId, UUID dispatcherId) {
        return dslContext.selectFrom(Tables.DISPATCHER)
                .where(Tables.DISPATCHER.ID.eq(dispatcherId))
                .and(Tables.DISPATCHER.CONTRACTOR_ID.eq(contractorId))
                .fetchOptional().map(dispatcherMapper::toModel);
    }

    @Override
    public Optional<Dispatcher> getByContractorIdAndOauthId(UUID contractorId, UUID oauthId) {
        return dslContext.selectFrom(Tables.DISPATCHER)
                .where(Tables.DISPATCHER.OAUTH_ID.eq(oauthId))
                .and(Tables.DISPATCHER.CONTRACTOR_ID.eq(contractorId))
                .fetchOptional().map(dispatcherMapper::toModel);
    }

    @Override
    public Optional<Dispatcher> get(UUID dispatcherId) {
        return dslContext.selectFrom(Tables.DISPATCHER)
                .where(Tables.DISPATCHER.ID.eq(dispatcherId))
                .fetchOptional().map(dispatcherMapper::toModel);
    }

    @Override
    public Optional<Dispatcher> getByOauthId(UUID oauthId) {
        return dslContext.selectFrom(Tables.DISPATCHER)
                .where(Tables.DISPATCHER.OAUTH_ID.eq(oauthId))
                .fetchOptional().map(dispatcherMapper::toModel);
    }

    @Override
    public List<Dispatcher> findAllByContractorIdAndIdIn(UUID contractorId, Set<UUID> ids) {
        return dslContext.selectFrom(Tables.DISPATCHER)
                        .where(Tables.DISPATCHER.CONTRACTOR_ID.eq(contractorId))
                        .and(Tables.DISPATCHER.ID.in(ids))
                        .fetchInto(DispatcherRecord.class)
                .stream().map(dispatcherMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Dispatcher> getAllByIds(List<UUID> ids) {
        return dslContext.selectFrom(Tables.DISPATCHER)
                .where(Tables.DISPATCHER.ID.in(ids))
                .fetchInto(DispatcherRecord.class)
                .stream().map(dispatcherMapper::toModel)
                .toList();
    }
}

