package ru.sberbank.ditsib.transport.request.controller.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.CheckinSettingsController;
import ru.sberbank.ditsib.transport.request.database.model.CheckinSettings;
import ru.sberbank.ditsib.transport.request.dto.CheckinSettingsDTO;
import ru.sberbank.ditsib.transport.request.service.CheckinSettingsService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of checkin controller service.
 */
@RequiredArgsConstructor
@RestController
@E2EController
class CheckinSettingsControllerImpl implements CheckinSettingsController {
    
    private final CheckinSettingsService checkinSettingsService;
    
    @Override
    public CheckinSettingsDTO save(CheckinSettingsDTO checkinSettingsDTO) {
    
        CheckinSettings checkinSettings = new CheckinSettings();
        checkinSettings.setServiceType(checkinSettingsDTO.getServiceType());
        checkinSettings.setTransportType(checkinSettingsDTO.getTransportType());
        checkinSettings.setRegion(checkinSettingsDTO.getRegion());
        checkinSettings.setRadius(checkinSettingsDTO.getRadius());
        checkinSettings = checkinSettingsService.save(checkinSettings);
        
        return checkinSettingsService.transformEntityToDTO(checkinSettings);
    }
    
    @Override
    public void delete(@NotNull UUID id) {
        checkinSettingsService.delete(id);
    }
    
    @Override
    public CheckinSettingsDTO get(@NotNull UUID id) {
        return checkinSettingsService.getDTO(id);
    }
    
    @Override
    public List<? extends CheckinSettingsDTO> getAll() {
        List<CheckinSettings> list = checkinSettingsService.getAll();
        return list.stream().map(checkinSettingsService::transformEntityToDTO).collect(Collectors.toList());
    }
}