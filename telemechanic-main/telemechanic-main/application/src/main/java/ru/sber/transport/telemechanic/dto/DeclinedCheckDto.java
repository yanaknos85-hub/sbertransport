package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(name = "DeclinedCheckDto", description = "Проверка с комментарием для отклонения заявки")
public record DeclinedCheckDto(
        @NotNull
        @Schema(description = "Идентификатор проверки")
        UUID id,
        @Schema(description = "Комментарий проверки")
        @Size(max = 255, message = "Комментарий должен быть не более 255 символов")
        String comment
) {
}
