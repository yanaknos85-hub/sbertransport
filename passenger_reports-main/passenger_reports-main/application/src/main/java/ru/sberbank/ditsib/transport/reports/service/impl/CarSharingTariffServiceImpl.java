package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.TariffMessage;
import ru.sberbank.ditsib.transport.reports.dao.CarsharingTariffRepository;
import ru.sberbank.ditsib.transport.reports.model.tariff.CarSharingTariff;
import ru.sberbank.ditsib.transport.reports.service.TariffService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class CarSharingTariffServiceImpl implements TariffService<CarSharingTariff> {
    
    private final CarsharingTariffRepository carsharingTariffRepository;
    
    @Override
    public CarSharingTariff save(CarSharingTariff tariff) {
        return carsharingTariffRepository.save(tariff);
    }
    
    @Override
    public CarSharingTariff updateOrCreate(TariffMessage message) {
        CarSharingTariff carSharingTariff = findById(message.getId()).orElse(null);
        if (carSharingTariff == null) {
            carSharingTariff = new CarSharingTariff();
            carSharingTariff.setId(message.getId());
        }
        carSharingTariff = carSharingTariff.toBuilder()
                                           .humanReadableId(message.getHumanReadableId())
                                           .transportType(getTransportType())
                                           .region(message.getRegion())
                                           .build();
        
        return save(carSharingTariff);
    }
    
    @Override
    public Optional<CarSharingTariff> findById(UUID id) {
        return carsharingTariffRepository.findById(id);
    }
    
    @Override
    public void deactivate(UUID id) {
        carsharingTariffRepository.deactivate(id);
    }
    
    @Override
    public TransportTypeEnum getTransportType() {
        return TransportTypeEnum.CARSHARING;
    }
}

