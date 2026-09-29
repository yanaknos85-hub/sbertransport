package ru.sberbank.ditsib.transport.tariff.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TaxiTariffSender;

@Slf4j
@RequiredArgsConstructor
@Component
public class TaxiTariffSenderImpl implements TaxiTariffSender {

    @Qualifier("taxiTariffOutput")
    private final ObjectProvider<OutputBridge> taxiTariffOutput;
    
    private final TariffMapper mapper;
    
    @Override
    public void send(TaxiTariff taxiTariff) {
        var message = mapper.toMessage(taxiTariff, false);
        log.info("TaxiTariffSender: going to send tariff {}", message.getId());
        taxiTariffOutput.ifAvailable(it -> it.send(message));
    }
    
    @Override
    public void sendDeleted(TaxiTariff taxiTariff) {
        var message = mapper.toMessage(taxiTariff, true);
        taxiTariffOutput.ifAvailable(it -> it.send(message));
    }
}
