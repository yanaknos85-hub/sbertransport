package ru.sberbank.ditsib.transport.vehicle.dto.files;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.transport.vehicle.constants.ReportType;

import java.util.UUID;

@Schema(title = "Фильтры для отчетов", description = "Фильтры для отчетов")
public record ReportQueryParametersDto(
        @NotNull
        @Schema(description = "Идентификатор записи об организации", requiredMode = Schema.RequiredMode.REQUIRED, type = "uuid",
                example = "e29294a1-b5b4-4174-8ed8-0c9b52f85a2b")
        UUID organizationId,
        @NotNull
        @Schema(description = "Отчетный год", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024")
        int year,
        @NotNull
        @Schema(description = "Вид отчета", requiredMode = Schema.RequiredMode.REQUIRED, example = "FUEL")
        ReportType reportType) {
}
