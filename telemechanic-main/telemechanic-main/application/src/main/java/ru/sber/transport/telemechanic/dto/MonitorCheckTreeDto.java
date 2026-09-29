package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckTypeMonitoring;

import java.util.List;
import java.util.UUID;

@With
@Schema(name = "MonitorCheckTreeDto", title = "Проверка для монитора")
public record MonitorCheckTreeDto(
        @Schema(description = "Идентификатор проверки")
        UUID id,
        @Schema(description = "Тип проверки")
        CheckTypeMonitoring checkType,
        @Schema(description = "Статус проверки")
        CheckStatus checkStatus,
        @Schema(description = "Количество попыток")
        int attempt,
        @Schema(description = "Максимальное количество попыток")
        int maxAttempt,
        List<UUID> photos,
        @Schema(description = "Комментарий")
        String comment,
        @Schema(description = "Дочерние проверки")
        List<MonitorCheckTreeDto> children
) {
}
