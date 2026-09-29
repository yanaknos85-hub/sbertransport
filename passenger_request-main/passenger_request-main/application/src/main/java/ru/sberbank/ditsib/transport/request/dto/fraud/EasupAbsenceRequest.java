package ru.sberbank.ditsib.transport.request.dto.fraud;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на проверку отсутствия сотрудника в ЕАСУП")
public record EasupAbsenceRequest(
        @Schema(description = "Дата, на которую проверяется отсутствие сотрудника")
        Long desiredDate,
        @Schema(description = "Часовой пояс сотрудника")
        String timeZone,
        @Schema(description = "Идентификатор сотрудника")
        String personnelNumber,
        @Schema(description = "Ожидаемая продолжительность поездки")
        Long expectedDuration
) {
}
