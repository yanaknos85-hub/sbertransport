package ru.sber.transport.notifications.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.messages.PushMessage;
import ru.sber.transport.notifications.messaging.senders.PushSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация отправителя PUSH.
 */
@RequiredArgsConstructor
@Component
class PushSenderImpl implements PushSender {

    @Qualifier("pushOutput")
    private final ObjectProvider<OutputBridge> pushOutput;

    @Qualifier("pushOutputSsl")
    private final ObjectProvider<OutputBridge> pushOutputSsl;


    @SuppressWarnings("java:S3958")
    @Override
    public void send(UUID id, List<UUID> receivers, NotificationType type, String template, Map<String, Object> data,
                     Map<String, Object> additionalData) {
        var message = PushMessage.builder()
                .id(id)
                .receivers(receivers)
                .type(type.name()).additionalData(additionalData)
                .template(template).data(data)
                .build();
        pushOutput.ifAvailable(ob -> ob.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
        pushOutputSsl.ifAvailable(ob -> ob.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }
}
