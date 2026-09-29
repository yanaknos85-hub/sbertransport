package ru.sber.transport.trip.providers.contractor.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.trip.database.trips.tables.records.ContractorsRecord;

/**
 * Маппер контрагентов.
 */
@Mapper
public interface ContractorMapper {

    ContractorsRecord toRecord(ContractorMessage message);

    @Mapping(target = "integrationType", expression = "java(mapIntegrationType(record.getIntegrationType(), message.integrationType()))")
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget ContractorsRecord record, ContractorMessage message);

    default String mapIntegrationType(String recordIntegrationType, String integrationType) {
        return integrationType == null ? recordIntegrationType : integrationType;
    }
}
