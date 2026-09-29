package ru.sberbank.ditsib.transport.request.dto.personal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(title = "Запрос на проверку дробления поездки", description = "Данные новой заявки")
public record PersonalTransportSplitCheckRqDTO(
        @Schema(description = "Желаемая дата поездки в миллисекундах с начала эпохи (Unix timestamp)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1672531200000")
        long desiredDate,

        @Schema(description = "Часовой пояс пассажира", requiredMode = Schema.RequiredMode.REQUIRED, example = "GMT+3")
        @NotNull
        String timeZone,

        @Schema(description = "Идентификатор сотрудника, инициировавшего поездку", requiredMode = Schema.RequiredMode.REQUIRED, example = "5c3cbe2f-5464-4c12-8176-7804fa7ade6f")
        @NotNull
        UUID employeeId,

        @Schema(description = "Ожидаемая стоимость поездки в копейках", requiredMode = Schema.RequiredMode.REQUIRED, example = "130000")
        long expectedCost,

        @Schema(description = "Ожидаемая продолжительность поездки в миллисекундах", requiredMode = Schema.RequiredMode.REQUIRED, example = "900000000")
        long expectedDuration
) {
}