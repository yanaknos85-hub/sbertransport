package ru.sber.transport.trip.providers.integration_client.mapper;

import org.mapstruct.Mapper;

import ru.sber.transport.dispatcher.messages.IntegrationClientMessage;
import ru.sber.transport.trip.business.model.IntegrationClient;
import ru.sber.transport.trip.database.trips.tables.records.IntegrationClientRecord;

@Mapper
public interface IntegrationClientMapper {

    IntegrationClientRecord toRecord(IntegrationClient client);

    IntegrationClient toModel(IntegrationClientRecord clientRecord);

    IntegrationClient toModel(IntegrationClientMessage message);

}
