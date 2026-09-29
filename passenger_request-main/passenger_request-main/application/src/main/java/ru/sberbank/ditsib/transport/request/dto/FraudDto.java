package ru.sberbank.ditsib.transport.request.dto;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Объект для передачи данных о фроде
 * @param comment комментарий анти-фрод системы
 */
@Schema(title = "Фрод", description = "Данные о фроде")
public record FraudDto(
        @Schema(description = "Комментарий анти-фрод системы", requiredMode = Schema.RequiredMode.REQUIRED)
        String comment
) {
}
