package ru.sber.transport.trips.cargo.messaging.providers;

import ru.sber.transport.trips.cargo.business.model.Vehicle;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * Провайдер автомобилей.
 */
public interface VehicleProvider {

    /**
     * Сохранение
     * @param vehicle автомобиль
     */
    int save(Vehicle vehicle);

    /**
     * Получить автомобиль по ID
     * @param id ID водителя
     */
    Optional<Vehicle> get(UUID id);
}
