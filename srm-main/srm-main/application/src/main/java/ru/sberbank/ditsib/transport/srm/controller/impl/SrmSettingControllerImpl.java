package ru.sberbank.ditsib.transport.srm.controller.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.srm.config.SrmSettingNames;
import ru.sberbank.ditsib.transport.srm.controller.SrmSettingController;
import ru.sberbank.ditsib.transport.srm.dto.SrmSettingDTO;
import ru.sberbank.ditsib.transport.srm.model.SrmSetting;
import ru.sberbank.ditsib.transport.srm.service.SrmSettingService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of srm controller service.
 */
@RequiredArgsConstructor
@RestController
class SrmSettingControllerImpl implements SrmSettingController {
    
    private final SrmSettingService srmSettingService;
    
    @Override
    public SrmSettingDTO save(SrmSettingDTO srmSettingDTO) {
        SrmSetting srmSetting = new SrmSetting();
        srmSetting.setName(srmSettingDTO.getName());
        srmSetting.setValue(srmSettingDTO.getValue());
        srmSetting = srmSettingService.save(srmSetting);
        
        return transformEntityToDTO(srmSetting);
    }
    
    @Override
    public void delete(@NotNull SrmSettingNames srmSettingsName) {
        srmSettingService.delete(srmSettingsName);
    }
    
    @Override
    public SrmSettingDTO get(@NotNull SrmSettingNames srmSettingsName) {
        SrmSetting srmSetting = srmSettingService.get(srmSettingsName);
        if (srmSetting == null) {
            throw new EntityNotFoundException(SrmSetting.class, srmSettingsName.name());
        }
        return transformEntityToDTO(srmSetting);
    }
    
    @Override
    public String getByName(@NotNull SrmSettingNames srmSettingsName) {
        return srmSettingService.getByName(srmSettingsName);
    }
    
    @Override
    public List<? extends SrmSettingDTO> getAll() {
        List<SrmSetting> list = srmSettingService.getAll();
        return list.stream().map(this::transformEntityToDTO).collect(Collectors.toList());
    }
    
    private SrmSettingDTO transformEntityToDTO(SrmSetting srmSetting) {
        if (srmSetting == null) {
            return null;
        }
        SrmSettingDTO srmSettingDTO = new SrmSettingDTO();
        srmSettingDTO.setName(srmSetting.getName());
        srmSettingDTO.setValue(srmSetting.getValue());
        return srmSettingDTO;
    }
}