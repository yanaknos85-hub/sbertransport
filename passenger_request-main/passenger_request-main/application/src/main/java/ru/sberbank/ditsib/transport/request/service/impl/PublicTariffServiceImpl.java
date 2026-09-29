package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.tariff.messaging.PublicTariffMessage;
import ru.sberbank.ditsib.transport.request.database.dao.PublicTariffRepository;
import ru.sberbank.ditsib.transport.request.database.model.PublicTariff;
import ru.sberbank.ditsib.transport.request.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.request.service.PublicTariffService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicTariffServiceImpl implements PublicTariffService {
    
    private final PublicTariffRepository publicTariffRepository;
    
    private final TariffMapper tariffMapper;
    
    @Override
    public PublicTariff save(PublicTariff tariff) {
        return publicTariffRepository.save(tariff);
    }
    
    @Override
    public void save(UUID id, PublicTariffMessage message) {
        var found = publicTariffRepository.findById(id).orElseGet(() -> createTariff(id));
        tariffMapper.update(found, message);
        publicTariffRepository.save(found);
    }
    
    @Override
    public Optional<PublicTariff> getOptionalById(UUID tariffId) {
        return publicTariffRepository.findById(tariffId);
    }
    
    @Override
    public PublicTariff getTariffById(UUID tariffId) throws EntityNotFoundException {
        return getOptionalById(tariffId).orElseThrow(
                () -> new EntityNotFoundException(PublicTariff.class, tariffId));
    }
    
    @Override
    public void deleteById(UUID tariffId) throws EntityNotFoundException {
        PublicTariff tariff = publicTariffRepository.findById(tariffId).orElseThrow(
                () -> new EntityNotFoundException(PublicTariff.class, tariffId));
        publicTariffRepository.delete(tariff);
    }
    
    @Override
    public void delete(PublicTariff tariff) {
        publicTariffRepository.delete(tariff);
    }
    
    private PublicTariff createTariff(UUID id) {
        var tariff = new PublicTariff();
        tariff.setId(id);
        return tariff;
    }
}
