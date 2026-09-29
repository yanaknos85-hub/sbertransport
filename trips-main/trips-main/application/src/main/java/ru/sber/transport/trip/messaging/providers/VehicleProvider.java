package ru.sber.transport.trip.messaging.providers;

import ru.sber.transport.trip.business.model.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Получить автомобиль по ID и идентификатору контрагента
     * @param id ID водителя
     * @param contractorId идентификатор контрагента
     */
    Optional<Vehicle> getByIdAndContractorId(UUID id, UUID contractorId);

    /**
     * Получить автомобили по списку ID
     * @param ids
     * @return Список автомобилей
     */
    List<Vehicle> getAllByIds(List<UUID> ids);

}
