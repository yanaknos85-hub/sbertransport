package ru.sber.transport.request.external.messaging.listeners.avro;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.PositionsProvider;
import ru.sber.transport.messages.corporate.avro.PositionMessage;
import ru.sber.transport.request.external.messaging.listeners.model.avro.PositionAvroData;

@RequiredArgsConstructor
public class PositionAvroListener implements Consumer<Message<PositionMessage>> {

    private final PositionsProvider positionsProvider;

    @Override
    public void accept(Message<PositionMessage> raw) {
        positionsProvider.save(new PositionAvroData(raw.getPayload()));
    }
}
