package ru.sberbank.ditsib.transport.tariff.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.PersonalTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;

import java.util.HashMap;

/**
 * Implementation of sender taxi tariffs.
 */
@RequiredArgsConstructor
@Component
class TariffSenderImpl implements TariffSender {
    
    @Qualifier("tariffOutput")
    private final ObjectProvider<OutputBridge> tariffOutput;
    
    private final TariffMapper tariffMapper;
    
    @Override
    public void send(BaseTariff baseTariff, boolean deleted) {
        var priceDetail = new HashMap<String, Object>();
        if (baseTariff.getTransportType() == TransportTypeEnum.PERSONAL) {
            var personalTariff = (PersonalTariff) baseTariff;
            priceDetail.put("trustIdx", personalTariff.getTrustIdx());
            priceDetail.put("distanceIncluded", personalTariff.getDistanceIncluded());
        } else if (baseTariff.getTransportType() == TransportTypeEnum.TAXI) {
            var taxiTariff = (TaxiTariff) baseTariff;
            priceDetail.put("distanceIncluded", taxiTariff.getDistanceIncluded());
        }
        
        var message = tariffMapper.toMessage(baseTariff, deleted, priceDetail);
         
         tariffOutput.ifAvailable(it -> it.send(message));
    }
}
