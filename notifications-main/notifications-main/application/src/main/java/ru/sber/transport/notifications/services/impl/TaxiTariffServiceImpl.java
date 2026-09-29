package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.tariff.messaging.TariffMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.dao.messages.tariff.TaxiTariffRepository;
import ru.sber.transport.notifications.database.model.tariff.TaxiTariff;
import ru.sber.transport.notifications.services.TariffService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class TaxiTariffServiceImpl implements TariffService<TaxiTariff> {

    private final TaxiTariffRepository taxiTariffRepository;
    
    @Override
    public TaxiTariff save(TaxiTariff taxiTariff) {
        return taxiTariffRepository.save(taxiTariff);
    }

    @Override
    public TaxiTariff updateOrCreate(UUID id, TariffMessage message) {
        var taxiTariff = findById(message.getId()).orElse(null);
        if (taxiTariff == null) {
            taxiTariff = new TaxiTariff();
        }
        taxiTariff = taxiTariff.toBuilder()
                .humanReadableId(message.humanReadableId())
                .transportType(getTransportType())
                .region(message.regionId().toString())
                .build();

        return save(taxiTariff);
    }

    @Override
    public Optional<TaxiTariff> findById(UUID id) {
        return taxiTariffRepository.findById(id);
    }
    
    @Override
    public void deactivate(UUID id) {
        taxiTariffRepository.deactivate(id);
    }

    @Override
    public TransportTypeEnum getTransportType() {
        return TransportTypeEnum.TAXI;
    }
}
