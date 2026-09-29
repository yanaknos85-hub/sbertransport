package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.PersonalCarRepository;
import ru.sberbank.ditsib.transport.reports.model.PersonalCar;
import ru.sberbank.ditsib.transport.reports.service.PersonalCarService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonalCarServiceImpl implements PersonalCarService {
    
    private final PersonalCarRepository repository;
    
    @Override
    public void delete(PersonalCar personalCar) {
        repository.delete(personalCar);
    }
    
    @Override
    public PersonalCar save(PersonalCar personalCar) {
        return repository.save(personalCar);
    }
    
    @Override
    public Optional<PersonalCar> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public PersonalCar findOrCreateById(UUID personalCarId) {
        return repository.findById(personalCarId).orElseGet(() -> repository.save(new PersonalCar(personalCarId)));
    }
}
