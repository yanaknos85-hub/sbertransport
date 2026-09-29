package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.database.model.DrivingLicense;

import java.util.UUID;

public interface DrivingLicenseService {
    
    void saveOrUpdate(DrivingLicense drivingLicense);
    
    DrivingLicense get(UUID id);
}
