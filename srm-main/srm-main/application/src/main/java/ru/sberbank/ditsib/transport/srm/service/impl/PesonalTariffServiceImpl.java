package ru.sberbank.ditsib.transport.srm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.srm.dao.PersonalTariffRepository;
import ru.sberbank.ditsib.transport.srm.model.tariff.PersonalTariff;
import ru.sberbank.ditsib.transport.srm.service.PersonalTariffService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PesonalTariffServiceImpl implements PersonalTariffService {
    
    private final PersonalTariffRepository personalTariffRepository;
    
    @Override
    public PersonalTariff save(PersonalTariff personalTariff) {
        return personalTariffRepository.save(personalTariff);
    }
    
    @Override
    public Optional<PersonalTariff> findById(UUID id) {
        return personalTariffRepository.findById(id);
    }
    
    @Override
    public void delete(PersonalTariff personalTariff) {
        personalTariffRepository.delete(personalTariff);
    }

}
