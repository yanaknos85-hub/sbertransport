package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.database.model.UpdateRequest;
import ru.sberbank.ditsib.transport.request.mappers.UpdateRequestMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.UpdateRequestSender;

/**
 * Реализация отправителя.
 */
@RequiredArgsConstructor
@Component
public class UpdateRequestSenderImpl implements UpdateRequestSender {

    @Qualifier("updateRequestOutput")
    private final ObjectProvider<OutputBridge> updateRequestOutput;

    private final UpdateRequestMapper mapper;

    @Override
    public void send(UpdateRequest update) {
        updateRequestOutput.ifAvailable(outputBridge -> outputBridge.send(
                mapper.toMessage(update)));
    }

    @Override
    public void sendDeleted(UpdateRequest update) {
        updateRequestOutput.ifAvailable(outputBridge -> outputBridge.send(
                mapper.toMessage(update, true)));
    }

}
