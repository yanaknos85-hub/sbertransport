package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.transport.vehicle.constants.TransportStatus;

import java.util.UUID;

@Schema(description = "DTO для результатов поиска Транспортного Средства")
public record TransportSearchResponseDto(
        @Schema(description = "ID записи", requiredMode = Schema.RequiredMode.REQUIRED, example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
        UUID id,
        @Schema(description = "Государственный номер", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 9, example = "A123AA777")
        String stateNumber,
        @Schema(description = "VIN", maxLength = 17, example = "WBA47110007817985")
        String vinCode,
        @Schema(description = "Статус", example = "В эксплуатации")
        TransportStatus status,
        @Schema(description = "ID контрагента (автопарка)", example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
        UUID contractorId,
        @Schema(description = "ID филиала автопарка", example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
        UUID autoparkId
) {
}
