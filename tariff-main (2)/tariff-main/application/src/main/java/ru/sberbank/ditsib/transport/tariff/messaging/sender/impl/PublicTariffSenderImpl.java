package ru.sberbank.ditsib.transport.tariff.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.tariff.database.model.PublicTariff;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.PublicTariffSender;

@Slf4j
@RequiredArgsConstructor
@Component
public class PublicTariffSenderImpl implements PublicTariffSender {
    
    @Qualifier("publicTariffOutput")
    private final ObjectProvider<OutputBridge> publicTariffOutput;
    
    private final TariffMapper mapper;
    
    @Override
    public void send(PublicTariff tariff) {
        var message = mapper.toMessage(tariff, false);
        log.info("PublicTariffSender: going to send tariff " + message.getId());
        publicTariffOutput.ifAvailable(it -> it.send(message));
    }
    
    @Override
    public void sendDeleted(PublicTariff tariff) {
        var message = mapper.toMessage(tariff, true);
        publicTariffOutput.ifAvailable(it -> it.send(message));
    }
}
