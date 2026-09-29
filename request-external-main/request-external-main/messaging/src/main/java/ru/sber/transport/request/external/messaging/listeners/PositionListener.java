package ru.sber.transport.request.external.messaging.listeners;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.PositionsProvider;
import ru.sber.transport.request.external.messaging.listeners.model.PositionData;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

@RequiredArgsConstructor
public class PositionListener implements Consumer<Message<PositionMessage>> {

    private final PositionsProvider positionsProvider;

    @Override
    public void accept(Message<PositionMessage> raw) {
        positionsProvider.save(new PositionData(raw.getPayload()));
    }
}
