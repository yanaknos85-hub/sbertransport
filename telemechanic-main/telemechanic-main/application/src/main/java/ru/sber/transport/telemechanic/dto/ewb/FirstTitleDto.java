package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Структура с подписью титула")
public record FirstTitleDto(
        
        @Schema(description = "Сформированный файл")
        String content,
        
        @Schema(description = "Имя файла")
        String fileName,
        
        @Schema(description = "Данные из формы")
        FirstTitleRequest firstTitleForm,
        
        @NotBlank
        @Schema(description = "Человекочитаемый идентификатор ЭПЛ")
        String humanReadableId,
        
        @Schema(description = "Дата и время формирования титула")
        LocalDateTime creationTime,
        
        @NotNull(message = "Ид ЭПЛ должен быть задан")
        UUID ewbUuid,

        @NotNull(message = "Тип титула должен быть задан")
        EwbTitleType titleType,

        @NotBlank(message = "Данные подписи должны быть предоставлены")
        @Size(min = 100, message = "Слишком короткая строка подписи")
        String signature
) {
}
