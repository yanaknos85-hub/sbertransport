package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Статусы проверок", description = "Данные по количеству проверок в каждом статусе")
public record ChecksStatus(
        @Schema(description = "Всего проверок", requiredMode = Schema.RequiredMode.REQUIRED)
        int total,
        @Schema(description = "Количество успешно выполненных проверок", requiredMode = Schema.RequiredMode.REQUIRED)
        int success,
        @Schema(description = "Количество не пройденных проверок", requiredMode = Schema.RequiredMode.REQUIRED)
        int decline,
        @Schema(description = "Количество проверок, находящихся в процессе", requiredMode = Schema.RequiredMode.REQUIRED)
        int progress
) {
}
