package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.PersonalCar;

import java.util.Optional;
import java.util.UUID;

public interface PersonalCarService {
    
    void delete(PersonalCar personalCar);
    
    PersonalCar save(PersonalCar personalCar);
    
    Optional<PersonalCar> findById(UUID id);

    PersonalCar findOrCreateById(UUID personalCarId);
}
