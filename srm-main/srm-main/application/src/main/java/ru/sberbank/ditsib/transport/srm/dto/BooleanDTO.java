package ru.sberbank.ditsib.transport.srm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Объект данных для передачи результата
 */
@Data
@AllArgsConstructor
@ToString
@Builder
@Schema(title = "Информация о результате", description = "Информация о результате")
public class BooleanDTO {
    
    /**
     * Результат обработки
     */
    private boolean result;
}
