package ru.sber.transport.telemechanic.dto.ewb.telemech_out;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "SendAndSaveTitleRequest", title = "Запрос на отправку подписанного титула")
public record SendAndSaveTitleRequest(
        @NotBlank(message = "Название файла не может быть пустым")
        @Schema(description = "Название файла", requiredMode = Schema.RequiredMode.REQUIRED)
        String fileName,
        @NotBlank(message = "Контент файла не может быть пустым")
        @Schema(description = "Контент файла", requiredMode = Schema.RequiredMode.REQUIRED)
        String file,
        @NotBlank(message = "Подпись не может быть пустой")
        @Schema(description = "Подпись", requiredMode = Schema.RequiredMode.REQUIRED)
        String signature,
        @NotNull(message = "Идентификатор ЭПЛ не может быть пустым")
        @Schema(description = "Идентификатор ЭПЛ", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID ewbId,
        @Schema(description = "Время создания титула")
        LocalDateTime creationTime
) {
}
