package ru.sber.transport.authentication.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.authentication.messaging.senders.BlackListSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.messages.BlackListMessage;

/**
 * Реализация отправителя токенов в ЧС.
 */
@RequiredArgsConstructor
@Component
public class BlackListSenderImpl implements BlackListSender {

    @Qualifier("blackListOutput")
    private final ObjectProvider<OutputBridge> blackListOutput;

    @Qualifier("blackListOutputSsl")
    private final ObjectProvider<OutputBridge> blackListOutputSsl;
    
    @Override
    public void send(String token) {
        var message = BlackListMessage.builder().token(token).build();
        blackListOutput.ifAvailable(ob -> ob.send(message));
        blackListOutputSsl.ifAvailable(ob -> ob.send(message));
    }
    
}
