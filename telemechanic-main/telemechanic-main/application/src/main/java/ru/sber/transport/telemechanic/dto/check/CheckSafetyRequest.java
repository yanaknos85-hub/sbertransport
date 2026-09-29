package ru.sber.transport.telemechanic.dto.check;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(name = "CheckSafetyRequest", title = "Запрос на проверку безопасности", description = "Запрос на проверку безопасности")
public record CheckSafetyRequest(
        @NotNull
        @Schema(description = "Список проверок")
        List<CheckSafetyDto> checks
) {}
