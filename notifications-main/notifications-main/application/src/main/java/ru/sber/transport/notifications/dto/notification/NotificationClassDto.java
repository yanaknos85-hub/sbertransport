package ru.sber.transport.notifications.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;

import java.util.List;

import static ru.sber.transport.notifications.database.model.settings.notification.NotificationType.*;
import static ru.sber.transport.notifications.database.model.settings.notification.NotificationType.COOP_TRIP_CHANGES;

/**
 * Возможные классы уведомлений.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Schema(title = "Возможные классы уведомлений (типы обращений)")
public enum NotificationClassDto {
    
    /**
     * Запрос на такси.
     */
    @Schema(description = "Заявка на такси")
    REQUEST_TAXI(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Запрос на общественный транспорт.
     */
    @Schema(description = "Заявка на общественный транспорт")
    REQUEST_PUBLIC(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Запрос на личный транспорт.
     */
    @Schema(description = "Заявка на личный транспорт")
    REQUEST_PERSONAL(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Запрос на каршеринг.
     */
    @Schema(description = "Заявка на каршеринг")
    REQUEST_CAR_SHARING(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Запрос на велосипед.
     */
    @Schema(description = "Заявка на велосипед")
    REQUEST_BICYCLE(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Запрос на самокат.
     */
    @Schema(description = "Заявка на самокат")
    REQUEST_SCOOTER(NotificationSettings.ParentType.ORGANIZATION),

    /**
     * Запрос на доставку груза
     */
    @Schema(description = "Запрос на доставку груза")
    REQUEST_CARGO(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Запрос на персональный лимит.
     */
    @Schema(description = "Заявка на личный лимит")
    REQUEST_LIMIT_PERSON(NotificationSettings.ParentType.ORGANIZATION),

    /**
     * Запрос на доставку тела
     */
    @Schema(description = "Запрос на трансфер")
    REQUEST_GROUP_TRANSFER(NotificationSettings.ParentType.ORGANIZATION),

    /**
     * Запрос на лимит подразделения.
     */
    @Schema(description = "Заявка на лимит подразделения")
    REQUEST_LIMIT_DEPARTMENT(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Событие лимита подразделения.
     */
    @Schema(description = "Лимит подразделения")
    LIMIT_DEPARTMENT(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Событие персонального лимита.
     */
    @Schema(description = "Личный лимит")
    LIMIT_PERSON(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Событие делегирования.
     */
    @Schema(description = "Делегаты")
    USER_DELEGATE(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Событие владельца лимита.
     */
    @Schema(description = "Владелец лимита")
    USER_OWNER_LIMIT(NotificationSettings.ParentType.ORGANIZATION),
    
    /**
     * Событие назначения.
     */
    @Schema(description = "Назначения")
    USER_ASSIGNMENT(NotificationSettings.ParentType.ORGANIZATION),

    /**
     * Событие водителя.
     */
    @Schema(description = "События водителя")
    CONTRACTOR(NotificationSettings.ParentType.CONTRACTOR),
    
    /**
     * События диспетчерской.
     */
    @Schema(description = "Уведомления диспетчерской")
    DISPATCHER_NOTIFICATION(NotificationSettings.ParentType.ORGANIZATION);

    private final NotificationSettings.ParentType parentType;
}
