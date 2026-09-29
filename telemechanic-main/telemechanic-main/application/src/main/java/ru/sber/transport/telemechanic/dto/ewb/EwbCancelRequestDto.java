package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Структура для отмены создаваемого ЭПЛ")
public record EwbCancelRequestDto(
        @Schema(description = "Комментарий диспетчера")
        @NotBlank
        @Size(min = 10, max = 255)
        String comment
) {
}
