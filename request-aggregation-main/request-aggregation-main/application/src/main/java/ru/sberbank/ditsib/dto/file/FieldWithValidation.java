package ru.sberbank.ditsib.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "FieldWithValidation", description = "Значение поля вместе с результатом его валидации")
public class FieldWithValidation<T> {
    @Schema(description = "Значение поля, введенное пользователем")
    private T value;
    @Schema(description = "Результат валидации значения поля")
    private boolean isError;
    @Schema(description = "Сообщение об ошибке валидации")
    private String errorMessage;
}
