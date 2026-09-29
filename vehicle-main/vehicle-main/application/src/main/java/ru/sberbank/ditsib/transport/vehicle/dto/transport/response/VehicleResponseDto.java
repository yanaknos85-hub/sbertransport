package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.transport.vehicle.constants.TransportStatus;

import java.util.UUID;

@Schema(description = "Автомобиль")
public record VehicleResponseDto(
        @Schema(description = "ID автомобиля", example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
        UUID id,
        @Schema(description = "Статус")
        TransportStatus status,
        @Schema(description = "Страна изготовитель", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50, example = "Россия")
        String manufacturer,
        @Schema(description = "VIN", maxLength = 17, example = "WBA47110007817985")
        String vinCode,
        @Schema(description = "Номер основного средства", maxLength = 50, example = "2423234234")
        String assetNumber,
        @Schema(description = "Инвентарный номер", maxLength = 50, example = "2423234234")
        String inventoryNumber,
        @Schema(description = "Номер кузова", maxLength = 17, example = "ВА5566435")
        String bodyNumber,
        @Schema(description = "Номер шасси", maxLength = 17, example = "АА33367896")
        String chassisNumber,
        @Schema(description = "Вид ТС", maxLength = 255, example = "Личный")
        String type,
        @Schema(description = "Подвид ТС", maxLength = 255, example = "СТС")
        String subtype,
        @Schema(description = "Привод", maxLength = 255, example = "Полный")
        String drive,
        @Schema(description = "Экологический класс", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 1, example = "1")
        String ecologicalClass,

        @Schema(description = "Тип кузова", requiredMode = Schema.RequiredMode.REQUIRED, example = "Седан")
        String bodyType,

        @Schema(description = "Тип трансмиссии", requiredMode = Schema.RequiredMode.REQUIRED, example = "МКПП 5")
        String transmissionType,

        @Schema(description = "Период выпуска модели", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-н.в.")
        String manufacturePeriod

) {
}
