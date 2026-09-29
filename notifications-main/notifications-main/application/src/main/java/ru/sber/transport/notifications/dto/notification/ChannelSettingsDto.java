package ru.sber.transport.notifications.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Данные уведомлений.
 */
@NoArgsConstructor
@Setter
@Getter
@Builder
@AllArgsConstructor
@Schema(title = "Данные об уведомлении", description = "Наполнение уведомления")
public class ChannelSettingsDto {
    
    /**
     * Канал отправки уведомлений.
     */
    @Schema(description = "Канал отправки уведомлений")
    @NotNull
    private Channel channel;
    
    /**
     * Текст уведомления.
     */
    @Schema(description = "Текст уведомлений")
    @NotBlank
    private String text;
    
    @Schema(description = "Активность канала. По-умолчанию канал активен")
    @Builder.Default
    private boolean enabled = true;
    
}
