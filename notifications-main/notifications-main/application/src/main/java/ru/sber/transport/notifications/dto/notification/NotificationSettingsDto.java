package ru.sber.transport.notifications.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Объект для клиента с данными настройки уведомлений.
 */
@NoArgsConstructor
@Setter
@Getter
@Schema(title = "Настройки уведомлений", description = "Данные с настройками уведомлений")
public class NotificationSettingsDto extends NewNotificationSettingsDto {
    
    /**
     * Тип уведомления.
     */
    @Schema(description = "Идентификатор настроек для изменения")
    @NotNull
    private UUID id;
    
}
