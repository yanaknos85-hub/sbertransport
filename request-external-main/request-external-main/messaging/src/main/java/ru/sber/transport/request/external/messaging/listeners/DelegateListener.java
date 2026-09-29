package ru.sber.transport.request.external.messaging.listeners;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;

@RequiredArgsConstructor
public class DelegateListener implements Consumer<Message<DelegateMessage>> {

    private final DelegatesProvider delegatesProvider;

    @Override
    public void accept(Message<DelegateMessage> raw) {
        final var payload = raw.getPayload();
        if (TransportTypeEnum.TAXI.getId().equals(payload.getTransportTypeId())) {
            delegatesProvider.save(new ru.sber.transport.request.external.messaging.listeners.model.DelegateData(payload));
        }
    }
}
