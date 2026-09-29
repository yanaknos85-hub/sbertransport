package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.CheckinSettings;
import ru.sberbank.ditsib.transport.request.dto.CheckinSettingsDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service for working with limit settings.
 */
public interface CheckinSettingsService {

    /**
     * Add limit setting.
     *
     * @param serviceType name.
     * @param transportType value.
     */
    CheckinSettings add(TransportServiceType serviceType,
                        TransportTypeEnum transportType,
                        UUID region,
                        Integer radius,
                        Boolean checkinOnlyManual);
    
    /**
     * Save limit setting.
     *
     * @param checkinSettings limit setting.
     */
    CheckinSettings save(CheckinSettings checkinSettings);
    
    /**
     * Delete limit setting.
     *
     * @param id limit setting.
     */
    void delete(UUID id);
    
    /**
     * Get limitSpending.
     *
     * @param id name of limitSpending.
     *
     * @return limit setting.
     */
    CheckinSettings get(UUID id);
    
    /**
     * Get limitSpending.
     *
     * @param id name of limitSpending.
     *
     * @return limit setting DTO.
     */
    CheckinSettingsDTO getDTO(UUID id);
    
    /**
     * Get limitSpending.
     *
     * @param serviceType name of limitSpending.
     *
     * @return limit setting.
     */
    CheckinSettings getByParams(
            TransportServiceType serviceType,
            TransportTypeEnum transportType,
            UUID region);
    
    /**
     * Get all limit settings.
     *
     *
     * @return list of limit setting.
     */
    List<CheckinSettings> getAll();
    
    /**
     * Map checkinSettings to DTO.
     *
     *  @param checkinSettings limit setting.
     *
     * @return DTO of limit setting.
     */
    CheckinSettingsDTO transformEntityToDTO(CheckinSettings checkinSettings);
}
