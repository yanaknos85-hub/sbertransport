package ru.sber.transport.request.external.messaging.listeners.avro;

import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.messages.corporate.avro.DelegateData;
import ru.sber.transport.request.external.messaging.listeners.model.avro.DelegateAvroData;

@RequiredArgsConstructor
public class DelegateAvroListener implements Consumer<Message<DelegateData>> {

    private final DelegatesProvider delegatesProvider;

    @Override
    public void accept(Message<DelegateData> raw) {
        final var payload = raw.getPayload();
        if (TAXI.name().equals(payload.getTransportType())) {
            delegatesProvider.save(new DelegateAvroData(payload));
        }
    }
}
