package ru.sber.transport.contractor.dto;

import io.swagger.v3.oas.annotations.media.*;

import java.io.*;

@Schema(title = "Данные для обновления", description = "Объект с данными для частичного обновления объекта")
public record PatchData(

        @Schema(description = "Название поля для изменения")
        String field,

        @Schema(description = "Значение поля для изменения")
        Serializable value
) {
}
