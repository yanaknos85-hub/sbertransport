package ru.sber.transport.contractor.service;

import ru.sber.transport.contractor.database.model.CargoPackage;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Service for working with cargo packages.
 */
public interface CargoPackageService {
    
    /**
     * Get package.
     *
     * @param contractorId id of contractor
     * @param uuid ID of package.
     *
     * @return package dto.
     */
    CargoPackage get(@NotNull UUID contractorId, @NotNull UUID uuid);
    
    /**
     * Save package
     *
     * @param newData new data of package.
     *
     * @return package dto.
     */
    CargoPackage save(CargoPackage newData);
    
    /**
     * Delete package.
     *
     * @param contractorId id of contractor
     * @param uuid         ID of package.
     * @return deleted package.
     */
    CargoPackage delete(@NotNull UUID contractorId, @NotNull UUID uuid);
    
    /**
     * Get all packages of contractor
     *
     * @param contractorId id of contractor
     * @return all packages
     */
    List<CargoPackage> getAllPackagesByContractorId(@NotNull UUID contractorId);
}
