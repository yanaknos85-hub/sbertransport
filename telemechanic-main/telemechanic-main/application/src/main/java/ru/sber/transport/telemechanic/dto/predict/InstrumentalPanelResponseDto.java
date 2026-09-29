package ru.sber.transport.telemechanic.dto.predict;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "Ответ модели dashboard", description = "Ответ модели dashboard")
public record InstrumentalPanelResponseDto(
        @Schema(description = "Статус запроса", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        int status,
        @Schema(description = "Статус прохождения проверки", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        boolean result,
        @Schema(description = "Список подробностей", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<ErrorPredictDto> detail) {
}
