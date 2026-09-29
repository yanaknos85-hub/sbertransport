package ru.sber.transport.notifications.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.messaging.senders.EmailSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.EmailMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация отправителя почты.
 */
@RequiredArgsConstructor
@Component
class EmailSenderImpl implements EmailSender {

    @Qualifier("emailOutput")
    private final ObjectProvider<OutputBridge> emailOutput;

    @Qualifier("emailOutputSsl")
    private final ObjectProvider<OutputBridge> emailOutputSsl;

    @Value("${default.link.alpha:null}")
    private String alphaLink;

    @Value("${default.link.sigma:null}")
    private String sigmaLink;

    @Value("${default.link.dzo:null}")
    private String dzoLink;

    @Override
    public void send(List<String> recipients, String subject, String template, Map<String, Object> data) {
        var newData = new HashMap<>(data);
        newData.put("alphaLink", alphaLink);
        newData.put("sigmaLink", sigmaLink);
        newData.put("dzoLink", dzoLink);
        var message = EmailMessage.builder().emails(recipients).subject(subject).template(template).data(newData).html(true)
                .build();
        emailOutput.ifAvailable(ob -> ob.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
        emailOutputSsl.ifAvailable(ob -> ob.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
    }
}
