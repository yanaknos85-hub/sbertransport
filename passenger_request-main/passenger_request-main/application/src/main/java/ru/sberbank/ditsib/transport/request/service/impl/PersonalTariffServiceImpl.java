package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.tariff.messaging.PersonalTariffMessage;
import ru.sberbank.ditsib.transport.request.database.dao.PersonalTariffRepository;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;
import ru.sberbank.ditsib.transport.request.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.request.service.PersonalTariffService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonalTariffServiceImpl implements PersonalTariffService {
    
    private final PersonalTariffRepository personalTariffRepository;
    
    private final TariffMapper tariffMapper;
    
    @Override
    public PersonalTariff save(PersonalTariff tariff) {
        return personalTariffRepository.save(tariff);
    }
    
    @Override
    public void save(UUID id, PersonalTariffMessage message) {
        var found = personalTariffRepository.findById(id).orElseGet(() -> createTariff(id));
        tariffMapper.update(found, message);
        personalTariffRepository.save(found);
    }
    
    @Override
    public Optional<PersonalTariff> getOptionalById(UUID tariffId) {
        return personalTariffRepository.findById(tariffId);
    }
    
    @Override
    public PersonalTariff getTariffById(UUID tariffId) throws EntityNotFoundException {
        return getOptionalById(tariffId).orElseThrow(
                () -> new EntityNotFoundException(PersonalTariff.class, tariffId));
    }
    
    @Override
    public void deleteById(UUID tariffId) throws EntityNotFoundException {
        PersonalTariff tariff = personalTariffRepository.findById(tariffId).orElseThrow(
                () -> new EntityNotFoundException(PersonalTariff.class, tariffId));
        personalTariffRepository.delete(tariff);
    }
    
    @Override
    public void delete(PersonalTariff tariff) {
        personalTariffRepository.delete(tariff);
    }
    
    private PersonalTariff createTariff(UUID id) {
        var tariff = new PersonalTariff();
        tariff.setId(id);
        return tariff;
    }
}
