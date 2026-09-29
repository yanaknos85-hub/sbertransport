package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.sber.transport.messaging.Message;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;

import java.util.HashMap;
import java.util.UUID;

/**
 * Базовый класс для отправки сообщений о заявках.
 *
 * @param <T> тип заявки
 */
@RequiredArgsConstructor
@Slf4j
abstract class BaseRequestSender<T extends Request> implements RequestSender<T> {
    private static final String TRANSPORT_TYPE = "transportType";
    private static final String MESSAGE_ID = "messageId";
    private static final String FRAUD_HANDLED = "fraudHandled";

    @Qualifier("requestOutput")
    private final ObjectProvider<OutputBridge> requestOutput;

    @Override
    public void send(T request, boolean deleted) {
        var headers = new HashMap<String, Object>();
        headers.put(TRANSPORT_TYPE, request.getTransportType().getName());
        headers.put(MESSAGE_ID, UUID.randomUUID());
        final var fraudData = request.getFraudData();
        if (fraudData != null) {
            headers.put(FRAUD_HANDLED, fraudData.stream()
                    .map(FraudData::getType)
                    .map(Enum::name)
                    .toList());
        }
        var message = toMessage(request, deleted);
        if (message != null) {
            requestOutput.ifAvailable(outputBridge -> outputBridge.send(message, headers));
            log.info("Отправлено сообщение в топик service.request по заявке с id = {}, humanReadableId = {} с признаком deleted = {}",
                    message.getId(), request.getHumanReadableId(), deleted);
        }
    }

    /**
     * Конвертация заявки в сообщение.
     *
     * @param request заявка
     * @param deleted признак удаления
     * @return сообщение
     */
    protected abstract Message<?> toMessage(T request, boolean deleted);
}
