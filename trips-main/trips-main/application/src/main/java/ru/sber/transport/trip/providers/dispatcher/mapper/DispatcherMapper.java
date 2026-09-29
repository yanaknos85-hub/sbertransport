package ru.sber.transport.trip.providers.dispatcher.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.trip.business.model.Dispatcher;
import ru.sber.transport.trip.database.trips.tables.records.DispatcherRecord;

import java.util.UUID;

@Mapper
public interface DispatcherMapper {

    DispatcherRecord toRecord(Dispatcher dispatcher);

    @Mapping(target = "contractorId", source = "contractorId")
    Dispatcher toModel(ContractorMessage.Dispatcher dispatcher, UUID contractorId);

    Dispatcher toModel(DispatcherRecord dispatcher);

    Dispatcher toModel(ContractorUpdateTripMessage.Dispatcher dispatcher);

}
