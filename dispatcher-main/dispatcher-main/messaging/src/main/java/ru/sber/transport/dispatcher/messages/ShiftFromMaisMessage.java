package ru.sber.transport.dispatcher.messages;

import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сообщение о смене из МАИС
 * @param id
 * @param routeId Идентификатор маршрута
 * @param driverPersonnelNumber Табельный номер водителя
 * @param stateNumber Госномер автомобиля
 * @param startDate Дата и время начала маршрута
 * @param endDate Дата и время окончания маршрута
 * @param action Признак действия
 */
public record ShiftFromMaisMessage(
        UUID id,
        String routeId,
        String driverPersonnelNumber,
        String stateNumber,
        LocalDateTime startDate,
        LocalDateTime endDate,
        ActionType action
) implements Message<UUID> {

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }

    @Override
    public UUID getId() {
        return id;
    }
}