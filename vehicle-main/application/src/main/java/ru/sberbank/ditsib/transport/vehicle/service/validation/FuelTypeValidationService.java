package ru.sberbank.ditsib.transport.vehicle.service.validation;

import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для валидации типа топлива
 */
public interface FuelTypeValidationService {

    /**
     * Проверка на существование типа топлива с указанным наименованием и типом двигателя
     *
     * @param title      наименование типа топлива
     * @param engineType тип двигателя
     */
    void checkIfFuelTypeAlreadyExists(String title, EngineType engineType);

    /**
     * Проверка, что наименования типов топлива не используются в других типах топлива
     *
     * @param fuelTypeNames список наименований типов топлива
     * @param fuelTypeId    идентификатор типа топлива
     */
    void checkFuelTypeNamesAlreadyInUse(List<String> fuelTypeNames, UUID fuelTypeId);
}
