package ru.sberbank.ditsib.transport.tariff.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.tariff.database.model.PersonalTariff;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.PersonalTariffSender;

/**
 * Implementation of sender taxi tariffs.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PersonalTariffSenderImpl implements PersonalTariffSender {

    @Qualifier("personalTariffOutput")
    private final ObjectProvider<OutputBridge> personalTariffOutput;

    private final TariffMapper mapper;

    @Override
    public void send(PersonalTariff tariff) {
        var message = mapper.toMessage(tariff, false);
        log.info("PersonalTariffSender: going to send tariff {}", message.getId());
        personalTariffOutput.ifAvailable(it -> it.send(message));
    }

    @Override
    public void sendDeleted(PersonalTariff tariff) {
        var message = mapper.toMessage(tariff, true);
        personalTariffOutput.ifAvailable(it -> it.send(message));
    }
}
