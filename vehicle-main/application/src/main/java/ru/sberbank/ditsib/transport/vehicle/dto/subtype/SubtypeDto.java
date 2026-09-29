package ru.sberbank.ditsib.transport.vehicle.dto.subtype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeDto;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Подвид ТС", description = "Подвид траспортного средства")
public record SubtypeDto(
        
        @NotNull
        @Schema(description = "ID Подвида ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Наименование подвида ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование подвида ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @NotNull(message = "Подвид ТС должен относится к какому-либо Виду ТС")
        @Schema(description = "Вид ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        TypeDto type

) {
}