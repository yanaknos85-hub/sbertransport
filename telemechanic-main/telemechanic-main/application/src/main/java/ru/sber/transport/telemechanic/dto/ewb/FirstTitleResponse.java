package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "FirstTitleResponse", title = "Ответ на запрос формирования первого титула")
public record FirstTitleResponse(
        @NotBlank
        @Schema(description = "Человекочитаемый идентификатор", requiredMode = REQUIRED, example = "PL-0000-00000000")
        String humanReadableId,
        @NotBlank
        @Schema(description = "Имя файла", requiredMode = REQUIRED)
        String fileName,
        @Schema(description = "Сформированный файл", requiredMode = REQUIRED)
        byte[] content,
        @Schema(description = "Время создания титула", requiredMode = REQUIRED)
        LocalDateTime creationTime
) {
}
