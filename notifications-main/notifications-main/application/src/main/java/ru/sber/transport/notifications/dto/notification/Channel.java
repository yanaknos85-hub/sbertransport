package ru.sber.transport.notifications.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;

/**
 * Список доступных каналов уведомлений.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Schema(title = "Доступные каналы уведомлений")
public enum Channel {
    
    /**
     * PUSH.
     */
    @Schema(description = "PUSH")
    PUSH(ChannelType.PUSH),
    
    /**
     * SMS.
     */
    @Schema(description = "SMS")
    SMS(ChannelType.SMS),
    
    /**
     * EMail.
     */
    @Schema(description = "E-mail")
    EMAIL(ChannelType.EMAIL);
    
    /**
     * Аналог значения для базы данных.
     */
    private final ChannelType model;
}
