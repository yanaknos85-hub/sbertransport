package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.dto.*;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Service for working with user attributes.
 */
public interface UserAttributesService {
    
    /**
     * Get user attributes with ID.
     *
     * @param userId ID of user to get.
     *
     * @return user attributes.
     */
    UserAttributesDTO get(@NotNull UUID userId);
    
    /**
     * Get default user attributes.
     *
     * @return default user attributes.
     */
    UserAttributesDTO getDefaultAttributes();
    
    /**
     * Edit user attributes attributes.
     *
     * @param userId ID of user to get.
     *
     * @return edited user attributes.
     */
    UserAttributesDTO update(@NotNull UUID userId, TaxiUIVisibilityDTO taxiUIVisibilityDTO);
    
    /**
     * Edit user attributes attributes.
     *
     * @param userId ID of user to get.
     *
     * @return edited user attributes.
     */
    UserAttributesDTO update(@NotNull UUID userId, PersonalUIVisibilityDTO personalUIVisibilityDTO);
    
    /**
     * Edit user attributes attributes.
     *
     * @param userId ID of user to get.
     *
     * @return edited user attributes.
     */
    UserAttributesDTO update(@NotNull UUID userId, PublicUIVisibilityDTO publicUIVisibilityDTO);
    
    /**
     * Edit user attributes attributes.
     *
     * @param userId ID of user to get.
     *
     * @return edited user attributes.
     */
    UserAttributesDTO update(@NotNull UUID userId, CarsharingUIVisibilityDTO carsharingUIVisibilityDTO);
}
