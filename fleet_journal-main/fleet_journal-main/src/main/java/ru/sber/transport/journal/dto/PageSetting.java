package ru.sber.transport.journal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

/**
 * Настройка разделения на страницы
 *
 * @param page Номер страницы
 * @param size Количество элементов на странице
 */
@Schema(title = "Настройка пагинации", description = "Настройка пагинации")
public record PageSetting(
        @Schema(description = "Номер страницы")
        @Positive
        int page,
        @Schema(description = "Количество элементов на странице")
        @Positive
        int size
) {
    /**
     * Конструктор для определения параметров по-умолчанию
     */
    public PageSetting() {
        this(0, 20);
    }
}
