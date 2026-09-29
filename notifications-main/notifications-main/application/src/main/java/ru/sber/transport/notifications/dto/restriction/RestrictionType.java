package ru.sber.transport.notifications.dto.restriction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictType;

/**
 * Возможные типы ограничений.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Schema(title = "Возможные типы ограничений")
public enum RestrictionType {
    
    /**
     * Позволить всем.
     */
    @Schema(description = "Отправлять уведомления всем сотрудникам")
    ALLOW_ALL(RestrictType.ALLOW_ALL),
    
    /**
     * Позволить всем, кроме...
     */
    @Schema(description = "Отправлять уведомления всем сотрудникам, кроме сотрудников из списка")
    ALLOW_BUT(RestrictType.ALLOW_BUT),
    
    /**
     * Не позволять никому, кроме...
     */
    @Schema(description = "Не отправлять уведомления никому, кроме сотрудников из списка")
    DENY_BUT(RestrictType.DENY_BUT),
    
    /**
     * Не позволять никому.
     */
    @Schema(description = "Не отправлять уведомления никому")
    DENY_ALL(RestrictType.DENY_ALL);
    
    /**
     * Аналог значений в базе.
     */
    private final RestrictType model;
}
