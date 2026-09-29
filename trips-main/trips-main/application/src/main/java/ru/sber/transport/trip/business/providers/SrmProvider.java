package ru.sber.transport.trip.business.providers;

import ru.sber.transport.trip.business.model.Trip;

import java.util.concurrent.CompletableFuture;

/**
 * Провайдер данных SRM.
 */
public interface SrmProvider {

    /**
     * Обновить поездку данными в SRM.
     *
     * @param trip поездка.
     * @return процесс выполнения асинхронного задания.
     */
    CompletableFuture<Trip> update(Trip trip);

}
