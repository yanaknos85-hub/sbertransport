package ru.sber.transport.telemechanic.dto.check;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

@Schema(name = "CheckSafetyDto", title = "Проверки безопасности", description = "Проверки безопасности")
public record CheckSafetyDto(
        @NotNull
        @Schema(description = "Тип проверки",
                example = "VEHICLE_NUMBER")
        CheckType checkType,
        
        @NotNull
        @Schema(description = "Статус проверки",
                example = "DONE")
        CheckStatus status
) {}
