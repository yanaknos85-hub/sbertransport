package ru.sber.transport.authentication.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.authentication.messaging.senders.EmailSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.EmailMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Реализация отправителя почты.
 */
@RequiredArgsConstructor
@Component
@Slf4j
class EmailSenderImpl implements EmailSender {

    @Qualifier("emailOutput")
    private final ObjectProvider<OutputBridge> emailOutput;

    @Qualifier("emailOutputSsl")
    private final ObjectProvider<OutputBridge> emailOutputSsl;
    
    @Override
    public void send(List<String> emails, String subject, String template, Map<String, Object> data) {
        var message = EmailMessage.builder().emails(emails).subject(subject).template(template).data(data).html(true)
                    .build();
        emailOutput.ifAvailable(ob -> ob.send(message));
        emailOutputSsl.ifAvailable(ob -> ob.send(message));
    }
}
