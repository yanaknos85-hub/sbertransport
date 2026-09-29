package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;


@Schema(description = "Структура для отправки второго титула")
public record SecondTitleDto(
        @Schema(description = "Имя файла")
        String fileName,
        @Schema(description = "Сформированный файл")
        String file,
        @NotBlank(message = "Данные подписи должны быть предоставлены")
        @Size(min = 100, message = "Слишком короткая строка подписи")
        String signature,
        @Schema(description = "Идентификатор ЭПЛ")
        UUID ewbId,
        @Schema(description = "Дата и время создания титула")
        LocalDateTime creationTime
) {
}
