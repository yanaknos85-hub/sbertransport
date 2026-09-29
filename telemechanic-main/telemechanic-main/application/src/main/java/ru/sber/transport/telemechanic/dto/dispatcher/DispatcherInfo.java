package ru.sber.transport.telemechanic.dto.dispatcher;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "DispatcherInfo", title = "Информация о диспетчере")
public record DispatcherInfo(
        @Size(max = 255)
        @Schema(description = "Табельный номер диспетчера",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "123456789",
                maximum = "255")
        String personnelNumber,
        @Size(max = 765)
        @Schema(description = "ФИО диспетчера",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Иванов Иван Иванович",
                maximum = "765")
        String fullName,
        @Size(max = 255)
        @Schema(description = "Организация владельца автопарка",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "ЦА",
                maximum = "255")
        String organizationName,
        @Size(max = 255)
        @Schema(description = "Подразделение владельца автопарка",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Диспетчерская",
                maximum = "255")
        String departmentName
) {}
