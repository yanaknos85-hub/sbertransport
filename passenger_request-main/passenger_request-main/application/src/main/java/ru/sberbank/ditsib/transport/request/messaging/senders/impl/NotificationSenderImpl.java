package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.ws.messages.WebSocketMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.NotificationSender;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationSenderImpl implements NotificationSender {

    @Qualifier("subscriptionOutput")
    private final ObjectProvider<OutputBridge> subscriptionOutput;

    @Override
    public void sendNotificationToSubscriber(WebSocketMessage event) {
        Optional.ofNullable(subscriptionOutput.getIfAvailable())
                .ifPresent(outputBridge -> outputBridge.send(event));
    }
}
