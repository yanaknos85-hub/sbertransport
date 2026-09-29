package ru.sber.transport.telemechanic.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(title = "Ответ на получение активной заявки телемеханика")
public record ActiveResponse(
        @Schema(description = "Идентификатор заявки телемеханика")
        @NotNull
        UUID id
) {
}
