package ru.sberbank.ditsib.transport.srm.service;

import ru.sberbank.ditsib.transport.srm.config.SrmSettingNames;
import ru.sberbank.ditsib.transport.srm.model.SrmSetting;

import java.util.List;

/**
 * Service for working with Srm settings.
 */
public interface SrmSettingService {

    /**
     * Add Srm setting.
     *
     * @param name name.
     * @param value value.
     */
    SrmSetting add(SrmSettingNames name, String value);
    
    /**
     * Save Srm setting.
     *
     * @param SrmSetting Srm setting.
     */
    SrmSetting save(SrmSetting SrmSetting);
    
    /**
     * Delete Srm setting.
     *
     * @param name Srm setting.
     */
    void delete(SrmSettingNames name);
    
    /**
     * Get SrmSpending.
     *
     * @param name name of SrmSpending.
     *
     * @return Srm setting.
     */
    SrmSetting get(SrmSettingNames name);
    
    /**
     * Get SrmSpending.
     *
     * @param name name of SrmSpending.
     *
     * @return Srm setting.
     */
    String getByName(SrmSettingNames name);
    
    /**
     * Get all Srm settings.
     *
     *
     * @return list of Srm setting.
     */
    List<SrmSetting> getAll();
}
