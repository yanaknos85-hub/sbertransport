package ru.sberbank.ditsib.transport.reports.dto.analytic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(title = "Данные общего аналитического отчета", description = "Данные общего аналитического отчета")
public class GeneralAnalyticalReportResponseDTO {
    
    @Schema(title = "Данные графиков", description = "Данные графиков общего аналитического отчета")
    List<ChartDTO> charts;
    
    public GeneralAnalyticalReportResponseDTO(List<ChartDTO> charts) {
        this.charts = charts;
    }
}
