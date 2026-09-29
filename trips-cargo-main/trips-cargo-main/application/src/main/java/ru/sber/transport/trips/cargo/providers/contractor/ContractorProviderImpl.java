package ru.sber.transport.trips.cargo.providers.contractor;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.providers.contractor.mapper.ContractorMapper;
import ru.sber.transport.trips_cargo.database.trips_cargo.Keys;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.Contractors;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.Trips;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.ContractorsRecord;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Transactional
@Component
@RequiredArgsConstructor
class ContractorProviderImpl implements ContractorProvider {

    private final DSLContext dslContext;

    private final ContractorMapper contractorMapper;

    @Override
    public int save(UUID id, ContractorMessage message) {
        var contractorsRecord = contractorMapper.toRecord(message);
        contractorsRecord.setId(id);
        return dslContext.insertInto(Tables.CONTRACTORS).set(contractorsRecord)
                .onConflict(Keys.PK_CONTRACTORS.getFields()).doUpdate().set(contractorsRecord).execute();
    }

    @Override
    public long nextDigit(UUID contractorId) {
        var foundDigit = dslContext.select(Trips.TRIPS.DIGIT_ID).from(Tables.TRIPS)
                .where(Tables.TRIPS.CONTRACTOR_ID.eq(contractorId)).orderBy(Tables.TRIPS.DIGIT_ID.desc()).limit(1)
                    .fetchOptional(Tables.TRIPS.DIGIT_ID).map(BigInteger::longValue).orElse(0L);
        return foundDigit + 1;
    }

    @Override
    public long getContractorDigitId(UUID contractorId) {
        return dslContext.select(Contractors.CONTRACTORS.DIGIT_ID).from(Tables.CONTRACTORS)
                .where(Tables.CONTRACTORS.ID.eq(contractorId)).fetchOptional(Tables.CONTRACTORS.DIGIT_ID)
                    .map(BigInteger::longValue).orElse(0L);
    }

    @Override
    public boolean isAutoassign(UUID contractorId) {
        var autoassign = dslContext.select(Tables.CONTRACTORS.AUTOASSIGN)
                .from(Tables.CONTRACTORS)
                .where(Tables.CONTRACTORS.ID.eq(contractorId))
                .fetchOptional();
        if(autoassign.isPresent()){
            return autoassign.get().value1();
        } else throw new EntityNotFoundException(ContractorsRecord.class, contractorId);
    }

    @Override
    public boolean checkContractorExistence(UUID contractorId) {
        return dslContext.select(Contractors.CONTRACTORS.ID).from(Tables.CONTRACTORS)
                .where(Contractors.CONTRACTORS.ID.eq(contractorId))
                .fetchOptional().isPresent();
    }
}
