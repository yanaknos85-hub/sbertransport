package ru.sber.transport.telemechanic.dto.telemedicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(title = "Поиск телемедицины", description = "Получение информации о прохождении телемедицины для монитора медика")
public record TelemedicineSearchResponse(
        @NotNull
        @Schema(description = "ID записи")
        UUID id,
        @NotBlank
        @Schema(description = "Номер телемедицины")
        String humanReadableId,
        @NotNull
        @Schema(description = "ID ЭПЛ")
        UUID ewbId,
        @NotBlank
        @Schema(description = "Номер путевого листа")
        String ewbHumanReadableId,
        @NotBlank
        @Schema(description = "Наименование организация")
        String organizationName,
        @NotNull
        @Schema(description = "Статус")
        String status,
        @NotNull
        @Schema(description = "Дата и время создания заявки")
        LocalDateTime creationTime,
        @NotBlank
        @Schema(description = "ФИО водителя")
        String driverFullName
) {
}
