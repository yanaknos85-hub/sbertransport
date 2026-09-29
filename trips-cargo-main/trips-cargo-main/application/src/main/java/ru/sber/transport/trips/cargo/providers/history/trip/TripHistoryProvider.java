package ru.sber.transport.trips.cargo.providers.history.trip;

import ru.sber.transport.trips.cargo.business.model.TripHistoryItem;

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

}
