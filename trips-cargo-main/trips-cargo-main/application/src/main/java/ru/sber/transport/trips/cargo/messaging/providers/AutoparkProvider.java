package ru.sber.transport.trips.cargo.messaging.providers;

import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.AutoparkRecord;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер для работы с филиалами
 */
public interface AutoparkProvider {

    /**
     * Сохранение филиала
     */
    int save(AutoparkMessage autoparkMessage);

    /**
     * Получение филиала по идентификатору маршрутизации
     * @param routingId идентификатор маршрутизации
     * @return филиал
     */
    Optional<AutoparkRecord> getByRoutingId(UUID routingId);
}
