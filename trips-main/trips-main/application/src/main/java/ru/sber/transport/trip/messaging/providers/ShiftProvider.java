package ru.sber.transport.trip.messaging.providers;

import ru.sber.transport.trip.business.model.Shift;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер смен.
 */
public interface ShiftProvider {

    /**
     * Сохранение
     * @param shift смена
     */
    int save(Shift shift);

    /**
     * Получение
     * @param id ID смены
     */
    Optional<Shift> get(UUID id);

    /**
     * Получение списка смен по ID водителя и текущей дате
     * @param driverId ID водителя
     * @param time текущая дата
     */
    List<Shift> getShiftByDriverIdAndCurrentDate(UUID driverId, LocalDateTime time);

    /**
     * Получение списка смен по ID автомобиля и текущей дате
     * @param vehicleId ID автомобиля
     * @param time текущая дата
     */
    List<Shift> getShiftByVehicleIdAndCurrentDate(UUID vehicleId, LocalDateTime time);

    /**
     * Получение списка смен по ID
     * @param ids список IDs
     * @return Список смен
     */
    List<Shift> getAllByIds(List<UUID> ids);

    /**
     * Получить смену по EWB ID
     * @param ewbId ID ЭПЛ
     * @return смена
     */
    Optional<Shift> getByEwbId(UUID ewbId);
}
