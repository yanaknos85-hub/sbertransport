package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "MedicRequestRegistryInfo", title = "Данные для реестра по заявке медецинского осмотра",
        description = "Данные по заявке медецинского осмотра для ответа формирования реестар ЭПЛ")
public record MedicRequestRegistryInfo(
        @Size(max = 36, message = "Не более 36 символов")
        @Schema(description = "Номер заявки",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "TL-0001-0000001",
                nullable = true,
                maximum = "36")
        String humanReadableId
) {
}
