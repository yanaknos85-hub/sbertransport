package ru.sberbank.ditsib.transport.reports.dto.analytic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Schema(title = "Аналитические данные", description = "Аналитические данные")
@SuperBuilder
@NoArgsConstructor
public class AnalysisReportDTO {
    @Schema(description = "Наименование сервиса")
    @Builder.Default
    private String serviceName = "Перевозка сотрудников";

    @Schema(description = "Тип транспорта")
    private String transportType;

    @Schema(description = "Подразделение первого уровня")
    private String organizationName;

    @Schema(description = "Подразделение второго уровня уровня")
    private String departmentName;

    @Schema(description = "Кол-во заявок по типу транспорта на текущую дату с начала года и до текущего числа")
    private long todayRequestsCount;

    @Schema(description = "Исполнение обязательств")
    private Rate fulfilmentRate;

    @Schema(description = "Отказы")
    private Rate declinedRate;

    @Schema(description = "Процент заявок с оценкой 4 и 5")
    private Rate csiRate;

    @Schema(description = "Просрочено по КС")
    private Rate expiredRate;

    @Schema(description = "%  данного (типа транспорта) в общих расходах на транспорт")
    private Rate transportTypeExpensesRate;

    @Schema(description = "% использования лимитов (по выбранному типу транспорта) от общих лимитов на год")
    private Rate limitSpentRate;

    @Schema(description = "Кол-во заявок по типу транспорта, с начала года и до текущего числа")
    private int  createdYearToDateCount;

    @Schema(description = "Кол-во заявок по типу транспорта, с начала года и до текущего числа, в завершенных статусах")
    private int  closedYearToDateCount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Rate {
        @Schema(description = "Количество по показателю")
        private long count;

        @Schema(description = "Процент к общему количеству")
        private int value;
    }
}

