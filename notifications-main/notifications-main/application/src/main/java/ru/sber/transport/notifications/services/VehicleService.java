package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.request.Vehicle;

import java.util.Optional;
import java.util.UUID;

public interface VehicleService {
    
    /**
     * Получение авто.
     *
     * @param vehicleId идентификатор авто.
     * @return авто.
     */
    Optional<Vehicle> get(UUID vehicleId);
    
    /**
     * Сохранение авто.
     *
     * @param vehicle авто.
     * @return сохраненный авто.
     */
    Vehicle save(Vehicle vehicle);
    
    /**
     * Удаление авто
     * @param vehicleId id авто
     */
    void deleteById(UUID vehicleId);
}
