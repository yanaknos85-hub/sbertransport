package ru.sber.transport.dispatcher.service;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.dto.AutoparkDTO;
import ru.sber.transport.dispatcher.dto.NewAutoparkDTO;
import ru.sber.transport.dispatcher.dto.VehicleNormDto;
import ru.sber.transport.dispatcher.dto.search.AutoparkSearchDTO;

import java.util.Collection;
import java.util.UUID;

/**
 * Service for working with autopark.
 */
public interface AutoparkService {

    /**
     * Add a new autopark to contractor.
     *
     * @param contractorId ID of contractor.
     * @param autopark     a new autopark data.
     * @return added autopark data.
     */
    AutoparkDTO add(UUID contractorId, NewAutoparkDTO autopark);

    /**
     * Edit autopark of contractor.
     *
     * @param contractorId ID of contractor.
     * @param autoparkId   ID of autopark.
     * @param autopark     new autopark data.
     */
    void edit(UUID contractorId, UUID autoparkId, NewAutoparkDTO autopark);

    /**
     * Delete autopark of contractor.
     *
     * @param contractorId ID of contractor.
     * @param autoparkId   ID of driver.
     */
    void delete(UUID contractorId, UUID autoparkId);

    /**
     * Delete all autoparks of contractor.
     * @param contractorId ID of contractor.
     */
    void deleteAllByContractorId(UUID contractorId);

    /**
     * Get autopark by ID.
     *
     * @param contractorId ID of contractor.
     * @param autoparkId   ID of autopark to get.
     * @return autopark.
     */
    AutoparkDTO get(UUID contractorId, UUID autoparkId);

    /**
     * Get all autopark DTOs of contractor.
     *
     * @param contractorId ID of contractor.
     * @param searchDTO autopark filters.
     * @return collections autoparks of contractor.
     */
    Page<AutoparkDTO> get(UUID contractorId, AutoparkSearchDTO searchDTO);

    /**
     * Get all autoparks of contractor.
     *
     * @param contractorId ID of contractor.
     * @return collections autoparks of contractor.
     */
    Collection<Autopark> getByContractorId(UUID contractorId);

    /**
     * Get autopark by name.
     *
     * @param name name of contractor.
     * @return autopark.
     */
    AutoparkDTO getByName(String name);

    /**
     * Get vehicle norm.
     * @param contractorId ID of contractor.
     * @return vehicle norm.
     */
    VehicleNormDto getVehicleNorm(@NonNull UUID contractorId);

    /**
     * Get autopark by id.
     * @param id ID of autopark.
     * @return autopark.
     */
    Autopark get(UUID id);
}
