package ru.sber.transport.telemechanic.dto.telemedicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@Schema(title = "Показания прохождения телемедицины", description = "Показания прохождения телемедицины")
public record DeclinedTelemedicineRequest(
        @NotNull
        @Positive
        @Min(40)
        @Max(300)
                @Schema(description = "Систолическое артериальное давление (мм рт. ст)")
        Integer systPressure,
        
        @NotNull
        @Positive
        @Min(40)
        @Max(300)
                @Schema(description = "Диастолическое артериальное давление (мм рт. ст)")
        Integer dyastPressure,
        
        @NotNull
        @Min(0)
        @Max(300)
        @Positive
                @Schema(description = "Пульс (уд./мин)")
        Integer pulse,
        
        @NotNull
        @Min(30)
        @Max(47)
        @Positive
                @Schema(description = "Температура (°С)")
        BigDecimal temperature,
        
        @NotNull
        @Min(0)
        @Max(1)
        @Schema(description = "Алкоголь в крови (Промилле)")
        BigDecimal bloodAlcohol,
        
        @NotBlank
        @Size(min = 1, max = 255)
                @Schema(description = "Комментарий")
        String comment
) {
}
