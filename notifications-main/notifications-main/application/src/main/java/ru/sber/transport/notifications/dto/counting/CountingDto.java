package ru.sber.transport.notifications.dto.counting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Количественный триггер отправки уведомлений.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(title = "Настройки отправки по количеству", description = "Количественный триггер отправки уведомлений")
public class CountingDto {
    
    /**
     * Число для достижения.
     */
    @Schema(description = "Значение счетчика для срабатывания отправки")
    @NotNull
    private Double value;
    
    /**
     * Тип числа.
     */
    @Schema(description = "Тип числа")
    @NotNull
    private CountType type;
    
    /**
     * Название свойства для расчёта срабатывания триггера.
     */
    @Schema(description = "Имя свойства для расчета срабатывания триггера")
    @NotBlank
    private String property;
    
}
