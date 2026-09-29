package ru.sber.transport.authentication.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import ru.sber.transport.authentication.messaging.senders.SmsSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.SmsMessage;

import java.util.Map;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Component
@Slf4j
public class SmsSenderImpl implements SmsSender {

    @Qualifier("smsOutput")
    private final ObjectProvider<OutputBridge> smsOutput;

    @Qualifier("smsOutputSsl")
    private final ObjectProvider<OutputBridge> smsOutputSsl;

    @Override
    public void send(String phone, String template, Map<String, Object> data) {
        var smsMessage = SmsMessage.builder()
                .phone(phone)
                .template(template)
                .data(data)
                .build();
        smsOutput.ifAvailable(ob -> ob.send(smsMessage, Map.of(KafkaHeaders.KEY, smsMessage.getId())));
        smsOutputSsl.ifAvailable(ob -> ob.send(smsMessage, Map.of(KafkaHeaders.KEY, smsMessage.getId())));
    }
}
