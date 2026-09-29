package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

import java.util.UUID;

@Schema(name = "Проверка", description = "Данные о проверке")
public record CheckDto(
        @Schema(description = "Идентификатор записи о проверке", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Тип проверки", requiredMode = Schema.RequiredMode.REQUIRED)
        CheckType checkType,
        @Schema(description = "Статус проверки", requiredMode = Schema.RequiredMode.REQUIRED)
        CheckStatus checkStatus,
        @Schema(description = "Количество попыток пройти проверку", requiredMode = Schema.RequiredMode.REQUIRED)
        int attempt,
        @Schema(description = "Порядковый номер проверки", requiredMode = Schema.RequiredMode.REQUIRED)
        int ordinal) {
}
