package ru.sber.transport.notifications.dto.restriction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Настройки ограничений.
 */
@NoArgsConstructor
@Setter
@Getter
@Schema(title = "Настройки ограничений", description = "Настройки ограничений уведомлений")
public class RestrictionDataDto {
    
    /**
     * Тип ограничения.
     */
    @Schema(description = "Тип ограничения")
    @NotNull
    private RestrictionType type;
    
    /**
     * Список исключений из ограничения.
     */
    @Schema(description = "Список ролей, исключенных из ограничения. Пустой список соответствует типам ограничений '_ALL'.")
    private List<String> roles = new ArrayList<>();
    
}
