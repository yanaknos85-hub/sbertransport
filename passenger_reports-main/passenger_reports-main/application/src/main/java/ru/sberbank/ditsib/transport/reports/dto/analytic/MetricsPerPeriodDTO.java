package ru.sberbank.ditsib.transport.reports.dto.analytic;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MetricsPerPeriodDTO {
    LocalDate dateFrom;
    LocalDate dateTo;
    List<MetricDTO> metrics;
}
