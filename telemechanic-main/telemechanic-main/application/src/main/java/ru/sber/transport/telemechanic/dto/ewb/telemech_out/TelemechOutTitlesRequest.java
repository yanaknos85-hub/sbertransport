package ru.sber.transport.telemechanic.dto.ewb.telemech_out;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "TelemechOutTitlesRequest", title = "Запрос на формирование титула")
public record TelemechOutTitlesRequest(
        @NotNull
        @Schema(description = "Идентификатор заявки телемеханика")
        UUID requestId,
        @NotNull
        @Schema(description = "Время допуска на линию от телемеханика")
        LocalDateTime decisionTime
) {}
