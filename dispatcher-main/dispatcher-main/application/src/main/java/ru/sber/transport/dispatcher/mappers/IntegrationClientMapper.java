package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.dispatcher.database.model.IntegrationClient;
import ru.sber.transport.dispatcher.messages.IntegrationClientMessage;

@Mapper
public interface IntegrationClientMapper {

    IntegrationClientMessage toMessage(IntegrationClient client);

}
