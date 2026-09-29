package ru.sber.transport.dispatcher.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.database.model.IntegrationClient;
import ru.sber.transport.dispatcher.mappers.IntegrationClientMapper;
import ru.sber.transport.dispatcher.messaging.senders.IntegrationClientSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

@Component
@RequiredArgsConstructor
public class IntegrationClientSenderImpl implements IntegrationClientSender {

    @Qualifier("integrationClient")
    private final ObjectProvider<OutputBridge> integrationClient;

    @Qualifier("integrationClientSsl")
    private final ObjectProvider<OutputBridge> integrationClientSsl;

    private final IntegrationClientMapper mapper;

    public void send(IntegrationClient client) {
        var message = mapper.toMessage(client);
        integrationClient.ifAvailable(o -> o.send(message));
        integrationClientSsl.ifAvailable(o -> o.send(message));
    }

}
