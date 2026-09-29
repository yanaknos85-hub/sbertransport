package ru.sber.transport.trips.cargo.providers.contractor.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.ContractorsRecord;

/**
 * Маппер контрагентов.
 */
@Mapper
public interface ContractorMapper {

    ContractorsRecord toRecord(ContractorMessage message);

}
