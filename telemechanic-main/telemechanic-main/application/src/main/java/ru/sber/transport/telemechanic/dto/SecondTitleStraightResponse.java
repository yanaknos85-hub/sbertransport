package ru.sber.transport.telemechanic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "SecondTitleStraightResponse", title = "Ответ получения информации о осмотре (прямой запрос)")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SecondTitleStraightResponse(
        @Schema(description = "Наименование файла Титула в соответствии с правилами формирования",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "uuid ЭПЛ",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID ewbUuid,
        @Schema(description = "Данные по медику",
                requiredMode = Schema.RequiredMode.REQUIRED)
        MedicInfo medicInfo,
        @Schema(description = "Данные о осмотре",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        ExamInfo exam
) {
    @Schema(description = "Модель данных по медику",
            requiredMode = Schema.RequiredMode.REQUIRED)
    public record MedicInfo(
            @Schema(description = "ФИО",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String fio,
            @Schema(description = "Табельный номер",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String personalNumber,
            @Schema(description = "Организация",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String organization,
            @Schema(description = "Подразделение",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String department,
            @Schema(description = "Должность",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String position,
            @Schema(description = "Номер электронного ключа медика",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String serialNumber,
            @Schema(description = "Дата окончания действия  электронного ключа медика",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDateTime serialEndDateTime) {
    }
    
    @Schema(description = "Модель данных по осмотру",
            requiredMode = Schema.RequiredMode.REQUIRED)
    public record ExamInfo(
            @Schema(description = "Признак прохождения осмотра",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            boolean medicRequestStatus,
            @Schema(description = "Дата и время создания заявки",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDateTime creationDateTime,
            @Schema(description = "Дата и время принятия решения медработником",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDateTime medicDecisionDateTime,
            @Schema(description = "Систолическое артериальное давление",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            int systPressure,
            @Schema(description = "Диастолическое  артериальное давление",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            int dyastPressure,
            @Schema(description = "Пульс",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            int pulse,
            @Schema(description = "Температура",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal temperature,
            @Schema(description = "Алкоголь промилле",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal alcohol,
            @Schema(description = "Комментарий (Причина недопуска)",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String comment
    ) {
    }
}
