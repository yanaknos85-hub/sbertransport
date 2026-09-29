package ru.sberbank.ditsib.transport.request.dto.fraud;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ от ЕАСУП об отсутствии сотрудника")
public record EasupAbsenceResponse(
        @Schema(description = "Тип отсутствия", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String type,
        @Schema(description = "Дата начала отсутствия", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long startDate,
        @Schema(description = "Дата окончания отсутствия", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long endDate
) {
}
