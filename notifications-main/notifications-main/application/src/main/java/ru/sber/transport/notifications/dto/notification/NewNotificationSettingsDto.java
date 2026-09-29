package ru.sber.transport.notifications.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.notifications.dto.counting.CountingDto;
import ru.sber.transport.notifications.dto.restriction.RestrictionDataDto;
import ru.sber.transport.notifications.dto.timing.TimingDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Объект от клиента с данными настройки уведомлений.
 */
@NoArgsConstructor
@Setter
@Getter
@Schema(title = "Настройки уведомлений", description = "Настройки уведомлений")
public class NewNotificationSettingsDto {
    
    /**
     * Класс уведомления.
     */
    @Schema(description = "Тип обращения")
    @NotNull
    private NotificationClassDto notificationClass;
    
    /**
     * Тип уведомления.
     */
    @Schema(description = "Тип уведомления")
    @NotNull
    private NotificationTypeDto notificationType;
    
    /**
     * Название уведомления.
     */
    @Schema(description = "Название уведомления")
    @NotBlank
    private String name;
    
    /**
     * Описание уведомления.
     */
    @Schema(description = "Описание уведомления")
    private String description;
    
    /**
     * Данные для настройки отправки уведомления.
     */
    @Schema(description = "Настройки уведомления")
    private List<@Valid ChannelSettingsDto> channels = new ArrayList<>();
    
    /**
     * Настройки отправки по времени.
     */
    @Schema(description = "Настройки отправки по времени")
    private List<@Valid TimingDto> timings = new ArrayList<>();
    
    /**
     * Настройки отправки по количеству.
     */
    @Schema(description = "Настройки отправки по количеству")
    private List<@Valid CountingDto> countings = new ArrayList<>();
    
    /**
     * Ограничения на отправку.
     */
    @Schema(description = "Ограничения на отправку")
    private RestrictionDataDto restriction;
    
}
