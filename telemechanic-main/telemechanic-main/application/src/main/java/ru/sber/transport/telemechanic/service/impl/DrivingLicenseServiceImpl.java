package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.DrivingLicenseRepository;
import ru.sber.transport.telemechanic.database.model.DrivingLicense;
import ru.sber.transport.telemechanic.exception.DrivingLicenseNotFoundException;
import ru.sber.transport.telemechanic.service.DrivingLicenseService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DrivingLicenseServiceImpl implements DrivingLicenseService {
    
    private final DrivingLicenseRepository drivingLicenseRepository;
    
    @Override
    @Transactional
    public void saveOrUpdate(DrivingLicense drivingLicense) {
        var entity = drivingLicenseRepository.findById(drivingLicense.getId());
        if (entity.isPresent()) {
            drivingLicenseRepository.save(entity.get()
                                                .setId(drivingLicense.getId())
                                                .setSeries(drivingLicense.getSeries())
                                                .setNumber(drivingLicense.getNumber())
                                                .setIssueDate(drivingLicense.getIssueDate())
                                                .setExpiryDate(drivingLicense.getExpiryDate())
                                                .setPreviousId(drivingLicense.getPreviousId())
                                         );
        } else {
            drivingLicenseRepository.save(drivingLicense);
        }
    }
    
    @Override
    @Transactional
    public DrivingLicense get(UUID id) {
        return drivingLicenseRepository.findById(id).orElseThrow(() -> new DrivingLicenseNotFoundException(id));
    }
}
