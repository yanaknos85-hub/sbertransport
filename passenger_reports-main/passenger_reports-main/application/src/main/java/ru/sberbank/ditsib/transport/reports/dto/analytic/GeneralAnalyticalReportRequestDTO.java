package ru.sberbank.ditsib.transport.reports.dto.analytic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@ToString
@Getter
@Setter
@Schema(title = "Параметры общего аналитического отчета", description = "Параметры и значения фильтров общего аналитического отчета")
public class GeneralAnalyticalReportRequestDTO {
    
    @Schema(description = "Организация")
    List<UUID> organizationId;
    
    @Schema(description = "Год отчета")
    Integer year;
    
    @Schema(description = "Месяц отчета")
    List<Integer> monthList;
    
    @Schema(description = "Виды транспорта")
    List<String> transportTypes;
}
