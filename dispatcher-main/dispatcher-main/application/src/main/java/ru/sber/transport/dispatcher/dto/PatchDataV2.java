package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.dispatcher.dto.enums.PatchField;

import java.io.Serializable;

/**
 * Данные для обновления
 * @param field поле
 * @param value значение
 */
@Schema(title = "Данные для обновления", description = "Объект с данными для частичного обновления объекта")
public record PatchDataV2(

        @Schema(description = "Название поля для изменения")
        PatchField field,

        @Schema(description = "Значение поля для изменения")
        Serializable value
) {
}
