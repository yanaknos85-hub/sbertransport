package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.contractor.Driver;

import java.util.Optional;
import java.util.UUID;

public interface DriverService {
    
    /**
     * Получение водителя.
     *
     * @param driverId идентификатор водителя.
     * @return водитель.
     */
    Optional<Driver> get(UUID driverId);
    
    /**
     * Сохранение Водителя.
     *
     * @param driver водитель.
     * @return сохраненный водитель.
     */
    Driver save(Driver driver);
    
    /**
     * Удаление водителя
     * @param driverId id водителя
     */
    void deleteById(UUID driverId);
}
