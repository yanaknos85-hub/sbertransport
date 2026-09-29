package ru.sber.transport.telemechanic.dto.ewb.second_title;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(title = "Форма для генерации титула №2", description = "Набор полей из формы для формирования титула")
public record SecondTitleForm(
        @Schema(description = "Идентификатор ЭПЛ")
        UUID ewbId,
        @Schema(description = "Показатели прохождения телемедицины")
        Request request
) {
    @Schema(name = "SecondTitleFormRequest")
    public record Request(
            @Schema(description = "Систолическое АР")
            int systPressure,
            @Schema(description = "Диастолическое АР")
            int dyastPressure,
            @Schema(description = "Пульс")
            int pulse,
            @Schema(description = "Температура")
            BigDecimal temperature,
            @Schema(description = "Алкоголь в крови")
            BigDecimal bloodAlcohol,
            @Schema(description = "Комментарий")
            String comment
    ) {
    }
}
