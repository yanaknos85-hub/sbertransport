package ru.sberbank.ditsib.transport.reports.dto.analytic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetricDTO {
    String code;
    String name;
    Long value;
    String typeValue;
}
