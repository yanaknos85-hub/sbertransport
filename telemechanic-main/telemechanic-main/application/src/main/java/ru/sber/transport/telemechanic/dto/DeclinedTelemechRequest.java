package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(name = "DeclinedTelemechRequest", title = "Запрос на отклонение заявки телемеханика с ЭПЛ")
public record DeclinedTelemechRequest(
        @Schema(description = "Комментарий к заявке")
        @Size(max = 255, message = "Комментарий должен быть не более 255 символов")
        String comment,
        @NotNull(message = "Проверки не могут быть null")
        @NotEmpty(message = "Проверки не могут быть пустыми")
        @Schema(description = "Проверки")
        Set<DeclinedCheckDto> checks
) {}
