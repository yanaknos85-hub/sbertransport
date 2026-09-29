package ru.sberbank.ditsib.transport.srm.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.srm.config.SrmSettingNames;
import ru.sberbank.ditsib.transport.srm.dao.SrmSettingRepository;
import ru.sberbank.ditsib.transport.srm.model.SrmSetting;
import ru.sberbank.ditsib.transport.srm.service.SrmSettingService;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SrmSettingServiceImpl implements SrmSettingService {
    
    @Autowired
    private SrmSettingRepository srmSettingRepository;
    
    public SrmSettingServiceImpl()   {}
    
    @Override
    @Transactional
    public SrmSetting add(SrmSettingNames name, String value) {
        SrmSetting srmSetting = new SrmSetting();
        srmSetting.setName(name);
        srmSetting.setValue(value);
        SrmSetting result;
        try {
            result = srmSettingRepository.save(srmSetting);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }
    
    @Override
    @Transactional
    public SrmSetting save(SrmSetting srmSetting) {
        try {
            SrmSetting srmSetting1 = srmSettingRepository.save(srmSetting);
            return srmSetting1;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void delete(SrmSettingNames name) {
        try {
            srmSettingRepository.delete(get(name));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public SrmSetting get(SrmSettingNames name) {
        return srmSettingRepository.findById(name).orElse(null);
    }
    
    @Override
    public String getByName(SrmSettingNames name) {
        return srmSettingRepository.findById(name).map(e -> e.getValue()).orElse(null);
    }
    
    @Override
    public List<SrmSetting> getAll() {
        return srmSettingRepository.findAll();
    }

}
