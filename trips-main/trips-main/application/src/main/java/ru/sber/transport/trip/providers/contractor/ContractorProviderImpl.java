package ru.sber.transport.trip.providers.contractor;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.dto.IntegrationType;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.Contractors;
import ru.sber.transport.trip.database.trips.tables.Trips;
import ru.sber.transport.trip.database.trips.tables.records.ContractorsRecord;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.providers.contractor.mapper.ContractorMapper;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Transactional
@Component
@RequiredArgsConstructor
class ContractorProviderImpl implements ContractorProvider {

    private final DSLContext dslContext;

    private final ContractorMapper contractorMapper;

    @Override
    public int save(UUID id, ContractorMessage message) {
        var contractorsRecord = new ContractorsRecord();
        var existingContractor = dslContext.selectFrom(Tables.CONTRACTORS)
                .where(Tables.CONTRACTORS.ID.eq(id))
                .fetchOptional();
        if(existingContractor.isPresent()){
            contractorsRecord = existingContractor.get();
            contractorMapper.update(contractorsRecord, message);
            return dslContext.update(Tables.CONTRACTORS).set(contractorsRecord).where(Tables.CONTRACTORS.ID.eq(id)).execute();
        } else {
            contractorsRecord = contractorMapper.toRecord(message);
            return dslContext.insertInto(Tables.CONTRACTORS).set(contractorsRecord).execute();
        }
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
    public boolean isDispatcher(UUID contractorId) {
        var integrationType = dslContext.select(Tables.CONTRACTORS.INTEGRATION_TYPE)
                .from(Tables.CONTRACTORS)
                .where(Tables.CONTRACTORS.ID.eq(contractorId))
                .fetchOptional();
        return integrationType.filter(stringRecord1 -> IntegrationType.DISPATCHER.name().equals(stringRecord1.value1())).isPresent();
    }

    @Override
    public long nextDigit(UUID contractorId) {
        var foundDigit = dslContext.select(Trips.TRIPS_.DIGIT_ID).from(Tables.TRIPS_)
                .where(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId)).orderBy(Tables.TRIPS_.DIGIT_ID.desc()).limit(1)
                    .fetchOptional(Tables.TRIPS_.DIGIT_ID).map(BigInteger::longValue).orElse(0L);
        return foundDigit + 1;
    }

    @Override
    public long getContractorDigitId(UUID contractorId) {
        return dslContext.select(Contractors.CONTRACTORS.DIGIT_ID).from(Tables.CONTRACTORS)
                .where(Tables.CONTRACTORS.ID.eq(contractorId)).fetchOptional(Tables.CONTRACTORS.DIGIT_ID)
                    .map(BigInteger::longValue).orElse(0L);
    }

    @Override
    public boolean checkContractorExistence(UUID contractorId) {
        return dslContext.select(Contractors.CONTRACTORS.ID).from(Tables.CONTRACTORS)
                .where(Contractors.CONTRACTORS.ID.eq(contractorId))
                .fetchOptional().isPresent();
    }

    @Override
    public List<ContractorsRecord> getContractorsByIds(List<UUID> contractorIds) {
        return dslContext.selectFrom(Tables.CONTRACTORS)
                .where(Tables.CONTRACTORS.ID.in(contractorIds))
                .fetchInto(ContractorsRecord.class);
    }
}
