package ru.sber.transport.trip.providers.history.trip;

import ru.sber.transport.trip.business.model.TripHistoryItem;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * Провайдер истории поездок.
 */
public interface TripHistoryProvider {

    /**
     * Сохранение
     * @param tripHistoryItem объект истории поездки
     */
    int save(TripHistoryItem tripHistoryItem);

    /**
     * Получение истории поездки по ID
     * @param tripId ID поездки
     * @return история поездки
     */
    List<TripHistoryItem> getHistoryByTripId(UUID tripId);

    /**
     * Получение истории поездок по списку ID
     * @param tripIds Список ID поездок
     * @return история поездок
     */
    List<TripHistoryItem> getHistoryByTripIds(List<UUID> tripIds);
}
