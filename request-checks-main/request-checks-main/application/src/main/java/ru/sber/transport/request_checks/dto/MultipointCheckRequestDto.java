package ru.sber.transport.request_checks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(title = "Запрос на проверку многоточечных поездок", description = "Данные для проверки лимита многоточечных поездок")
public record MultipointCheckRequestDto(
    @Schema(description = "ID пассажира", requiredMode = Schema.RequiredMode.REQUIRED, example = "611daf70-25e6-3844-a434-3583e234ac1b")
    @NotNull
    UUID passengerId,

    @Schema(description = "Дата-время поездки в ISO 8601 формате", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05-26T10:30:00")
    @NotNull
    LocalDateTime desiredDate,

    @Schema(description = "Часовой пояс в формате ISO 8601", example = "+03:00")
    @NotNull
    String timeZone
) {

}