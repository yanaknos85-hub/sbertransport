package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO для результатов поиска транспортного средства сотрудника, с добавлением транспортных средств по его штатной структуре")
public record TransportSearchWithStructureResponseDto(
        @Schema(description = "Идентификатор записи о транспортном средстве", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
        UUID id,
        @Schema(description = "Модель", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Outback")
        String model,
        @Schema(description = "Марка", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Subaru")
        String brand,
        @Schema(description = "Государственный номер", maxLength = 9, requiredMode = Schema.RequiredMode.REQUIRED,
                example = "A123AA777")
        String stateNumber
) {
}