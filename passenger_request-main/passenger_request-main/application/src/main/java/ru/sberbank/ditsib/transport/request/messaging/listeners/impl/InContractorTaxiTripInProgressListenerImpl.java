package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sberbank.ditsib.transport.request.service.InProgressMessageProcessor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Обработчик сообщений от сервиса app_passenger_integrations
 */
@Slf4j
@RequiredArgsConstructor
public class InContractorTaxiTripInProgressListenerImpl implements Consumer<Message<InContractorTaxiTripInProgressMessage>> {
    private final List<InProgressMessageProcessor> inProgressMessageProcessors;

    public void accept(Message<InContractorTaxiTripInProgressMessage> message) {
        handle(message.getHeaders().getId(), message.getPayload());
    }

    private void handle(UUID key, InContractorTaxiTripInProgressMessage message) {
        if (message.humanId() == null) {
            log.error("Message is not processed (messageKey={}): humanReadableId is null", key);
            return;
        }
        log.info("Received update for trip (messageKey={}, humanReadableId={})", key, message.humanId());
        inProgressMessageProcessors.stream()
                .filter(it -> Objects.equals(message.transportType(), it.transportType()))
                .forEach(it -> it.process(message));
    }
}
