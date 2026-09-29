package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.tariff.messaging.CarSharingTariffMessage;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingTariffRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTariff;
import ru.sberbank.ditsib.transport.request.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.request.service.CarsharingTariffService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarsharingTariffServiceImpl implements CarsharingTariffService {
    
    private final CarsharingTariffRepository carsharingTariffRepository;
    
    private final TariffMapper tariffMapper;
    
    @Override
    public CarsharingTariff save(CarsharingTariff tariff) {
        return carsharingTariffRepository.save(tariff);
    }
    
    @Override
    public void save(UUID id, CarSharingTariffMessage message) {
        var found = carsharingTariffRepository.findById(id).orElseGet(() -> createTariff(id));
        tariffMapper.update(found, message);
        carsharingTariffRepository.save(found);
    }
    
    @Override
    public Optional<CarsharingTariff> getOptionalById(UUID tariffId) {
        return carsharingTariffRepository.findById(tariffId);
    }
    
    @Override
    public CarsharingTariff getTariffById(UUID tariffId) throws EntityNotFoundException {
        return getOptionalById(tariffId).orElseThrow(
                () -> new EntityNotFoundException(CarsharingTariff.class, tariffId));
    }
    
    @Override
    public void deleteById(UUID tariffId) throws EntityNotFoundException {
        CarsharingTariff tariff = carsharingTariffRepository.findById(tariffId).orElseThrow(
                () -> new EntityNotFoundException(CarsharingTariff.class, tariffId));
        carsharingTariffRepository.delete(tariff);
    }
    
    @Override
    public void delete(CarsharingTariff tariff) {
        carsharingTariffRepository.delete(tariff);
    }
    
    private CarsharingTariff createTariff(UUID id) {
        var tariff = new CarsharingTariff();
        tariff.setId(id);
        return tariff;
    }
}
