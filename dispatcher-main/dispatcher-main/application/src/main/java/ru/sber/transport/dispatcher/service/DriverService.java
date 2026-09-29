package ru.sber.transport.dispatcher.service;

import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.dto.DriverDTO;
import ru.sber.transport.dispatcher.dto.NewDriverDTO;
import ru.sber.transport.dispatcher.dto.VehicleDTO;
import ru.sber.transport.dispatcher.dto.enums.PatchField;
import ru.sber.transport.dispatcher.dto.search.DriverSearchDTO;
import ru.sber.transport.dispatcher.messages.Source;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with driver.
 */
public interface DriverService {
    
    /**
     * Add a new driver to contractor.
     *
     * @param contractorId ID of contractor.
     * @param driver a new driver data.
     *
     * @return added driver data.
     */
    DriverDTO add(UUID contractorId, NewDriverDTO driver);
    
    /**
     * Edit driver of contractor.
     *
     * @param contractorId ID of contractor.
     * @param driverId ID of driver.
     * @param driver new driver data.
     */
    void edit(UUID contractorId, UUID driverId, NewDriverDTO driver);

    /**
     * Delete driver of contractor.
     * @param contractorId ID of contractor.
     * @param driverId ID of driver.
     */
    void delete(UUID contractorId, UUID driverId);

    /**
     * Delete all drivers of contractor.
     * @param contractorId ID of contractor.
     */
    void deleteAllByContractorId(UUID contractorId);

    /**
     * Get driver.
     *
     * @param driverId ID of driver.
     *
     * @return driver.
     */
    Optional<Driver> get(UUID driverId);

    /**
     * Save driver.
     *
     * @param driver data of driver.
     */
    void save(Driver driver);

    /**
     * Get driver of contractor.
     *
     * @param contractorId ID of contractor.
     * @param driverId ID of driver.
     *
     * @return driver.
     */
    DriverDTO get(UUID contractorId, UUID driverId);
    
    /**
     * Get all driver of contractor.
     *
     * @param contractorId ID of contractor.
     *
     * @return driver of contractor.
     */
    Page<DriverDTO> get(UUID contractorId, @Valid DriverSearchDTO searchDTO);

    /**
     * Получить текущий автомобиль водителя.
     *
     * @param driver водитель.
     * @return автомобиль
     */
    VehicleDTO getSelfVehicle(Driver driver);

    /**
     * Подписание ПДн.
     *
     * @param driverId идентификатор водителя.
     */
    void signPdn(@NonNull UUID driverId);

    /**
     * Частичное изменение водителя.
     *
     * @param id идентификатор водителя.
     * @param data данные для частичного изменения.
     */
    void patchDriver(UUID id, Map<PatchField, Serializable> data);

    /**
     * Get driver by oauth id.
     *
     * @param id oauth ID of driver.
     *
     * @return driver.
     */
    Optional<Driver> findByOauthId(UUID id);
}
