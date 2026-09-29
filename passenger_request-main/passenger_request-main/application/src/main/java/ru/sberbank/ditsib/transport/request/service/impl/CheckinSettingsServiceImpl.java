package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.CheckinSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.CheckinSettings;
import ru.sberbank.ditsib.transport.request.dto.CheckinSettingsDTO;
import ru.sberbank.ditsib.transport.request.service.CheckinSettingsService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CheckinSettingsServiceImpl implements CheckinSettingsService {
    
    private final CheckinSettingsRepository checkinSettingsRepository;
    
    @Override
    @Transactional
    public CheckinSettings add(TransportServiceType serviceType,
                               TransportTypeEnum transportType,
                               UUID region,
                               Integer radius,
                               Boolean checkinOnlyManual) {
        CheckinSettings checkinSettings = new CheckinSettings();
        checkinSettings.setServiceType(serviceType);
        checkinSettings.setTransportType(transportType);
        checkinSettings.setRegion(region);
        checkinSettings.setRadius(radius);
        checkinSettings.setCheckinOnlyManual(checkinOnlyManual);
        CheckinSettings result;
        try {
            result = checkinSettingsRepository.save(checkinSettings);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }
    
    @Override
    @Transactional
    public CheckinSettings save(CheckinSettings checkinSettings) {
        try {
            return checkinSettingsRepository.save(checkinSettings);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void delete(UUID id) {
        try {
            checkinSettingsRepository.delete(get(id));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public CheckinSettings get(UUID id) {
        return checkinSettingsRepository.getById(id);
    }
   
    @Override
    public CheckinSettingsDTO getDTO(UUID id) {
        CheckinSettings checkinSettings = checkinSettingsRepository.findById(id)
                                                                   .orElseThrow(() -> new EntityNotFoundException(CheckinSettings.class, id));
        return transformEntityToDTO(checkinSettings);
    }
    
    @Override
    public CheckinSettingsDTO transformEntityToDTO(CheckinSettings checkinSettings) {
        if (checkinSettings == null) {
            return null;
        }
        CheckinSettingsDTO checkinSettingsDTO = new CheckinSettingsDTO();
        checkinSettingsDTO.setServiceType(checkinSettings.getServiceType());
        checkinSettingsDTO.setTransportType(checkinSettings.getTransportType());
        checkinSettingsDTO.setRegion(checkinSettings.getRegion());
        checkinSettingsDTO.setRadius(checkinSettings.getRadius());
        return checkinSettingsDTO;
    }
    
    @Override
    public CheckinSettings getByParams(TransportServiceType serviceType,
                               TransportTypeEnum transportType,
                               UUID region) {
        return checkinSettingsRepository.findByServiceTypeAndTransportTypeAndRegion(
                                                                    serviceType, transportType, region).orElse(null);
    }
    
    @Override
    public List<CheckinSettings> getAll() {
        return checkinSettingsRepository.findAll();
    }

}
