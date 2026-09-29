package ru.sber.transport.notifications.dto.counting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.sber.transport.notifications.database.model.settings.counting.CountingType;

/**
 * Типы счетчика для триггера отправки.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Schema(title = "Возможные типы счетчика для триггера отправки")
public enum CountType {
    
    /**
     * При достижении значения.
     */
    @Schema(description = "Достижение значения")
    EXACT(CountingType.EXACT),
    
    /**
     * При достижении остатка.
     */
    @Schema(description = "Достижение остатка")
    REMAINS(CountingType.REMAINS),
    
    /**
     * При достижении определенного процента.
     */
    @Schema(description = "Достижение процента")
    PERCENT(CountingType.PERCENT);
    
    /**
     * Аналог значения для базы.
     */
    private final CountingType model;
    
}
