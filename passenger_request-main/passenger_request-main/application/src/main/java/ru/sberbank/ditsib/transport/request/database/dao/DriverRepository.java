package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;

import java.util.List;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
    
    List<Driver> findByLastNameAndFirstNameAndPatronymicAndContactPhoneAndActive(String lastName,
                                                                                 String firstName,
                                                                                 String patronymic,
                                                                                 String contactPhone,
                                                                                 boolean active);
}
