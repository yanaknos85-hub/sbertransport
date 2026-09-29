package ru.sber.transport.notifications.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import ru.sber.transport.messages.sms.avro.Data;
import ru.sber.transport.messages.sms.avro.DataList;
import ru.sber.transport.notifications.messaging.senders.SmsSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.SmsMessage;

import java.util.*;

/**
 * Реализация отправителя PUSH.
 */
@RequiredArgsConstructor
@Component
class SmsSenderImpl implements SmsSender {

    @Qualifier("sms")
    private final ObjectProvider<OutputBridge> smsOutputAvro;

    @Override
    public void send(List<String> phones, String template, Map<String, Object> data) {
        final var messageId = UUID.randomUUID();
        smsOutputAvro.ifAvailable(ob -> ob.send(ru.sber.transport.messages.sms.avro.SmsMessage.newBuilder()
                .setId(messageId)
                .setData(toData(data))
                .setPhones(phones)
                .setTemplate(template)
                .build()));
    }

    private DataList toData(Map<String, Object> data) {
        return DataList.newBuilder()
                .setValues(data.entrySet().stream().map(this::mapToData).toList())
                .build();
    }

    private Data mapToData(Map.Entry<?, ?> source) {
        final var value = source.getValue();
        final Object effectiveValue;
        if (value instanceof List<?> list) {
            effectiveValue = listToData(list);
        } else if (value instanceof Map<?, ?> map) {
            effectiveValue = DataList.newBuilder().setValues(map.entrySet().stream().map(this::mapToData).toList()).build();
        } else {
            effectiveValue = value;
        }
        return Data.newBuilder().setKey(String.valueOf(source.getKey())).setValue(effectiveValue).build();
    }

    private DataList listToData(List<?> source) {
        final var result = DataList.newBuilder();
        for (final var item : source) {
            final var index = String.valueOf(source.indexOf(item));
            final var values = Optional.ofNullable(result.getValues()).orElseGet(ArrayList::new);
            if (item instanceof List<?> list) {
                values.add(Data.newBuilder().setKey(index).setValue(listToData(list)).build());
            } else if (item instanceof Map<?, ?> map) {
                values.add(Data.newBuilder().setKey(index).setValue(DataList.newBuilder().setValues(map.entrySet().stream().map(this::mapToData).toList()).build()).build());
            } else {
                values.add(Data.newBuilder().setKey(index).setValue(item).build());
            }
            result.setValues(values);
        }
        return result.build();
    }
}
