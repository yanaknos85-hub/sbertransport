package ru.sber.transport.notifications.dto.timing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Типы срабатывания отправки.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Schema(title = "Возможные типы срабатывания отправки")
public enum EventType {
    
    /**
     * Во время события.
     */
    @Schema(description = "Отправка во время события")
    AT_EVENT(ru.sber.transport.notifications.database.model.settings.timing.EventType.AT_EVENT),
    
    /**
     * Перед контрольным сроком.
     */
    @Schema(description = "Отправка перед контрольным сроком")
    DEADLINE(ru.sber.transport.notifications.database.model.settings.timing.EventType.BEFORE_DEADLINE);
    
    /**
     * Аналог значения в базе данных.
     */
    private final ru.sber.transport.notifications.database.model.settings.timing.EventType model;
    
}
