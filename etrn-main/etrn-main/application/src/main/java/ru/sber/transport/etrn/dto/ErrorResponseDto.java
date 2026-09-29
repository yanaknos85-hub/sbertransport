package ru.sber.transport.etrn.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Универсальный DTO для ошибок API.
 *
 * @param errorCode    код ошибки
 * @param errorMessage описание ошибки на русском языке
 */
@Schema(title = "Ошибка API", description = "Информация об ошибке при обработке запроса")
public record ErrorResponseDto(

        @Schema(title = "Код ошибки", description = "Уникальный код ошибки", example = "ATTORNEY_CHECK_ERROR")
        String errorCode,

        @Schema(title = "Описание ошибки", description = "Описание ошибки на русском языке", example = "Ошибка при проверке доверенности")
        String errorMessage
) {
}
