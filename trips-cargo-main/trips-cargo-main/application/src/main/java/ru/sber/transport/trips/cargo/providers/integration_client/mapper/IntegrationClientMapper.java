package ru.sber.transport.trips.cargo.providers.integration_client.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.dispatcher.messages.IntegrationClientMessage;
import ru.sber.transport.trips.cargo.business.model.IntegrationClient;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.IntegrationClientRecord;

@Mapper
public interface IntegrationClientMapper {

    IntegrationClientRecord toRecord(IntegrationClient client);

    IntegrationClient toModel(IntegrationClientRecord clientRecord);

    IntegrationClient toModel(IntegrationClientMessage message);

}
