package ru.sber.transport.notifications.dto.timing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;

/**
 * Настройки тайминга отправки уведомлений.
 */
@NoArgsConstructor
@Setter
@Getter
@Builder
@AllArgsConstructor
@Schema(title = "Настройки тайминга", description = "Настройки тайминга отправки  уведомлений")
public class TimingDto {
    
    @Schema(description = "Имя поля для работы тайминга")
    private String timeFieldName;
    
    /**
     * Время до события.
     */
    @Schema(description = "Сколько времени должно остаться до события. По-умолчанию, в момент срабатывания",
            defaultValue = "0")
    @JsonDeserialize(using = MillisDurationConverter.class)
    @JsonSerialize(using = DurationMillisConverter.class, nullsUsing = DurationMillisConverter.class)
    @Builder.Default
    private Duration timeBefore = Duration.ZERO;
    
    /**
     * Событие отправки.
     */
    @Schema(description = "Событие отправки")
    @NotNull
    private EventType eventType;


    @Schema(description = "Имя поля для работы дэдлайна тайминга")
    private String deadlineFieldName;
}
