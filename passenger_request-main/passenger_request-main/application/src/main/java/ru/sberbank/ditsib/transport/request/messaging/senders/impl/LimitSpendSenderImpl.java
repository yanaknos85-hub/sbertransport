package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.mappers.LimitsMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.LimitSpendSender;


/**
 * Реализация отправителя.
 */
@RequiredArgsConstructor
@Component
public class LimitSpendSenderImpl implements LimitSpendSender {

    @Qualifier("limitSpendOutput")
    private final ObjectProvider<OutputBridge> limitSpendOutput;

    private final LimitsMapper mapper;

    @Override
    public void spend(Request request,
                      Integer sum,
                      boolean isCoop,
                      boolean isDriver,
                      TransportTypeEnum transportType,
                      Integer moneySaved) {
        limitSpendOutput.ifAvailable(outputBridge -> outputBridge.send(
                mapper.toSpendMessage(request, sum, isCoop, isDriver, transportType, moneySaved)));
    }
}
