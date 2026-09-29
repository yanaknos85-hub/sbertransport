package ru.sber.transport.telemechanic.dto.telemedicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(title = "TelemedicineResultRequest", description = "Запрос на добавление результата медицинского осмотра")
public record TelemedicineResultRequest(
        @Schema(description = "Наименование титула №2")
        String name,
        @NotNull
        @Schema(description = "Идентификатор ЭПЛ в системе КОРУС")
        UUID ewbUuid,
        @Schema(description = "Информация о медицинском работнике")
        MedicInfo medicInfo,
        @Schema(description = "Информация об осмотре")
        ExamInfo exam
) {
}
