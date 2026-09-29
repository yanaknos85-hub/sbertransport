package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "DTO Транспортного средства")
public record TransportResponseDto(
        @Schema(description = "ID записи", requiredMode = Schema.RequiredMode.REQUIRED, example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
        UUID id,
        @Schema(description = "Государственный номер", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 9, example = "A123AA777")
        String stateNumber,
        @Schema(description = "Марка", maxLength = 255, example = "Opel")
        String brand,
        @Schema(description = "Модель", maxLength = 255, example = "Vectra")
        String model,
        @Schema(description = "Год выпуска", minLength = 4, maxLength = 4, example = "2010")
        String year,
        @Schema(description = "Текущий пробег", maxLength = 6, example = "28600")
        int currentMileage,
        @Schema(description = "ID должности для закрепления ТС", example = "02f9a32a-7c76-4776-b60f-f6ec8b371a5d")
        UUID accessiblePositionId,
        @Schema(description = "Автомобиль")
        VehicleResponseDto vehicle,
        @Schema(description = "Организации")
        List<OrganizationResponseDto> organizations,
        @Schema(description = "Расположение")
        LocationResponseDto location,
        @Schema(description = "Комментарий",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "Комментарий",
                nullable = true,
                maximum = "255")
        @Size(max = 255, message = "Комментарий не должен превышать 255 символов")
        String comment,
        @Schema(description = "Документы")
        DocumentsResponseDto documents,
        @Schema(description = "Двигатель")
        EngineResponseDto engine,
        @Schema(description = "Общие характеристики")
        GeneralResponseDto general,
        @Schema(description = "Обслуживание")
        ServiceResponseDto service,
        @Schema(description = "Идентификатор контрагента (автопарка)")
        UUID contractorId,
        @Schema(description = "Идентификатор филиала автопарка")
        UUID autoparkId,
        @Schema(description = "Балансовая единица")
        String balanceUnitNumber,
        @Schema(description = "Завод")
        String facility,
        @Schema(description = "Единица оборудования")
        String equipmentUnitSystemNumber,
        @Schema(description = "Идентификатор подптипа ТС")
        UUID subtypeId,
        @Schema(description = "Идентификатор типа ТС")
        UUID typeId
) {
}
