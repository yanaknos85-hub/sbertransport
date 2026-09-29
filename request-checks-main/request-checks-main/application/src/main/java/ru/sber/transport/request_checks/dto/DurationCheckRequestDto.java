package ru.sber.transport.request_checks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(title = "Запрос на проверку длительности поездки", description = "Данные для проверки лимита длительности поездки")
public record DurationCheckRequestDto(
    @Schema(description = "ID пассажира", requiredMode = Schema.RequiredMode.REQUIRED, example = "4eafd718-927f-44f9-bf6c-665cc1b1a2be")
    @NotNull
    UUID passengerId,

    @Schema(description = "Желаемая дата и время поездки", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05-26T10:30:00+03:00")
    @NotNull
    OffsetDateTime desiredDate,

    @Schema(description = "Ожидаемая продолжительность поездки в миллисекундах", requiredMode = Schema.RequiredMode.REQUIRED, example = "3600000")
    @NotNull
    Long expectedDuration,

    @Schema(description = "Часовой пояс в формате ISO 8601", example = "+03:00")
    @NotNull
    String timeZone
) {

}
