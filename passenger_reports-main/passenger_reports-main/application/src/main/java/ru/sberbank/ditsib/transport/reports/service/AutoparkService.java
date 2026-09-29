package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.Position;
import ru.sberbank.ditsib.transport.reports.model.driversData.Autopark;

import java.util.Optional;
import java.util.UUID;

public interface AutoparkService {
    
    /**
     * Get autopark by ID.
     *
     * @param id ID of autopark.
     *
     * @return autopark.
     */
    Optional<Autopark> get(UUID id);
    
    /**
     * Delete autopark.
     *
     * @param autopark autopark entity.
     */
    void delete(Autopark autopark);
    
    /**
     * Save autopark.
     *
     * @param autopark autopark to save.
     */
    Autopark save(Autopark autopark);
    
    /**
     * Find or create autopark.
     *
     * @param id id of autopark to find.
     */
    Autopark findOrCreateAutoparkById(UUID id);
    
}
