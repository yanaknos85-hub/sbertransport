package ru.sber.transport.request.external.messaging.listeners;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.request.external.messaging.listeners.model.OrganizationData;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

@RequiredArgsConstructor
public class OrganizationListener implements Consumer<Message<OrganizationMessage>> {

    private final OrganizationsProvider organizationsProvider;

    @Override
    public void accept(Message<OrganizationMessage> raw) {
        organizationsProvider.save(new OrganizationData(raw.getPayload()));
    }
}
