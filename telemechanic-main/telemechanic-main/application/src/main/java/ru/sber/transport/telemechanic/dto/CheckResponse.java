package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

@Schema(title = "Ответ на прохождение проверки", description = "Ответ на прохождение проверки телемеханика")
public record CheckResponse(
        @Schema(description = "Статус проверки, которая пришла в запросе",
                example = "DONE",
                requiredMode = Schema.RequiredMode.REQUIRED)
        CheckStatus checkStatus,
        @Schema(description = "Тип следующей проверки",
                example = "WIND_SCREEN",
                requiredMode = Schema.RequiredMode.REQUIRED)
        CheckType nextCheck
) {
}
