package ru.sber.transport.telemechanic.dto.ewb.fifth_title;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "FifthTitleRequest", description = "Запрос на формирование пятого титула")
public record FifthTitleRequest(
        @Schema(description = "Идентификатор ЭПЛ")
        UUID id,
        @Schema(description = "Дата и время принятия решения на формирование пятого титула")
        LocalDateTime decisionTime
) {}
