package ru.sberbank.ditsib.transport.vehicle.dto.transport.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.sberbank.ditsib.transport.vehicle.constants.TransportStatus;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;

import java.util.List;
import java.util.UUID;

@Schema(description = "Запрос на поиск транспортных средств")
@Builder
public record TransportSelfSearchingRequestDto(
        @Schema(description = "Параметры пагинации", requiredMode = Schema.RequiredMode.REQUIRED)
        PageSettingDto page,

        @Schema(description = "Гос.номер авто или VIN-номер", minLength = 1, maxLength = 20)
        @Size(min = 1, max = 20)
        String searchText,

        @Schema(description = "Госномер автомобиля")
        String stateNumber,

        @Schema(description = "Идентификатор подразделения")
        UUID departmentId,

        @Schema(description = "Статус транспортного средства")
        TransportStatus status,

        @Schema(description = "Марка авто")
        UUID brand,

        @Schema(description = "Модель авто")
        UUID model,

        @Schema(description = "Год выпуска авто")
        Integer year,

        @Schema(description = "Идентификатор филиала автопарка")
        UUID autoparkId,

        @Schema(description = "Список идентификаторов автопарков")
        List<UUID> contractorIds
) {
}
