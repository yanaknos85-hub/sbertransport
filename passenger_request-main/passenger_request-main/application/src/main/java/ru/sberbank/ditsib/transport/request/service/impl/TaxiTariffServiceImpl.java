package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.request.database.dao.TaxiTariffRepository;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.request.service.TaxiTariffService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxiTariffServiceImpl implements TaxiTariffService {
    
    private final TaxiTariffRepository taxiTariffRepository;
    
    private final TariffMapper tariffMapper;
    
    @Override
    public TaxiTariff save(TaxiTariff tariff) {
        return taxiTariffRepository.save(tariff);
    }
    
    @Override
    public void save(UUID id, TaxiTariffMessage message) {
        var found = taxiTariffRepository.findById(id).orElseGet(() -> createTariff(id));
        tariffMapper.update(found, message);
        taxiTariffRepository.save(found);
    }
    
    @Override
    public Optional<TaxiTariff> getOptionalById(UUID tariffId) {
        return taxiTariffRepository.findById(tariffId);
    }
    
    @Override
    public TaxiTariff getTariffById(UUID tariffId) throws EntityNotFoundException {
        return getOptionalById(tariffId).orElseThrow(
                () -> new EntityNotFoundException(TaxiTariff.class, tariffId));
    }
    
    @Override
    public void deleteById(UUID tariffId) throws EntityNotFoundException {
        TaxiTariff tariff = taxiTariffRepository.findById(tariffId).orElseThrow(
                () -> new EntityNotFoundException(TaxiTariff.class, tariffId));
        taxiTariffRepository.delete(tariff);
    }
    
    @Override
    public void delete(TaxiTariff tariff) {
        taxiTariffRepository.delete(tariff);
    }
    
    private TaxiTariff createTariff(UUID id) {
        var tariff = new TaxiTariff();
        tariff.setId(id);
        return tariff;
    }
}
