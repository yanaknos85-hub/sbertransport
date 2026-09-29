package ru.sberbank.ditsib.transport.reports.dto.analytic;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChartDTO {
    String type;
    List<MetricDTO> data;
    List<MetricDTO> totals;
    List<MetricsPerPeriodDTO> dataPerPeriod;
}
