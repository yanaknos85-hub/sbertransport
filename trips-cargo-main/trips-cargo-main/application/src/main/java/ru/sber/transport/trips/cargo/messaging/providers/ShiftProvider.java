package ru.sber.transport.trips.cargo.messaging.providers;

import ru.sber.transport.trips.cargo.business.model.Shift;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

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
     * Получить смену по EWB ID
     * @param ewbId ID ЭПЛ
     * @return смена
     */
    Optional<Shift> getByEwbId(UUID ewbId);

}
