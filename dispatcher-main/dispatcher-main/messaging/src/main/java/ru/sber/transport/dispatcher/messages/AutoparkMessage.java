package ru.sber.transport.dispatcher.messages;

import ru.sber.transport.messaging.Message;

import java.util.*;

/**
 * Сообщение автопарка.
 *
 * @param id идентификатор.
 * @param name название.
 * @param deleted флаг удаления.
 * @param contractorId контрагент.
 * @param routingId id для маршрутизации поездок.
 * @param vehicleCountNorm нормативное количество автомобилей.
 */
public record AutoparkMessage(
        UUID id,
        String name,
        boolean deleted,
        UUID contractorId,
        UUID routingId,
        Integer vehicleCountNorm
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
