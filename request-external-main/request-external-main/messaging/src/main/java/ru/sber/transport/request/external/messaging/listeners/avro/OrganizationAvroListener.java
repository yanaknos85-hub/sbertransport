package ru.sber.transport.request.external.messaging.listeners.avro;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.messages.corporate.avro.OrganizationMessage;
import ru.sber.transport.request.external.messaging.listeners.model.avro.OrganizationAvroData;

@RequiredArgsConstructor
public class OrganizationAvroListener implements Consumer<Message<OrganizationMessage>> {

    private final OrganizationsProvider organizationsProvider;

    @Override
    public void accept(Message<OrganizationMessage> raw) {
        organizationsProvider.save(new OrganizationAvroData(raw.getPayload()));
    }
}
