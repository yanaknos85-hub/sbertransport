package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * DTO для ответа с информацией о заявке и результатом её валидации перед публикацией.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestValidationResponse {

    /**
     * Краткая информация о заявке
     */
    @Schema(description = "Информация о заявке")
    private RequestShortDto request;

    /**
     * Результат валидации заявки на публикацию
     */
    @Schema(description = "Результат проверки валидности заявки для публикации")
    private ValidationInfo validation;

    @Schema(description = "Флаг: успешность выполнения операции")
    private boolean isSuccess;

    @Schema(description = "Код и описание ошибки выполнения операции")
    private ErrorDetails error;


    /**
     * Вложенный объект: результат валидации
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationInfo {
        @Schema(description = "Флаг: может ли заявка быть опубликована")
        @JsonProperty("isValidForPublication")
        private Boolean isValidForPublication;

        @Schema(description = "Список ошибок валидации", implementation = ValidationError.class)
        private List<ValidationError> errors;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ValidationError {
            @Schema(description = "Поле, в котором возникла ошибка", example = "waypoints")
            private String field;

            @Schema(description = "Описание ошибки", example = "Минимум 2 точки маршрута (погрузка и выгрузка) должны быть указаны")
            private String message;
        }
    }

    /**
     * Вложенный объект: результат валидации
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetails {
        @Schema(description = "ФКод и описание ошибки выполнения операции")
        private String type;

        @Schema(description = "Список ошибок валидации")
        private String message;
    }
}

