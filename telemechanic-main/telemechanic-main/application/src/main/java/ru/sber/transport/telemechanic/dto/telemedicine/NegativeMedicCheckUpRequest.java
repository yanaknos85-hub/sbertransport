package ru.sber.transport.telemechanic.dto.telemedicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Негативный результат прохождения медосмотра", title = "Негативный результат прохождения медосмотра")
public record NegativeMedicCheckUpRequest(
        @Schema(description = "Данные по медику",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        MedicInfo medicInfo,
        @Schema(description = "Данные о осмотре",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        ExamInfo exam
) {
}
