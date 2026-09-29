package ru.sberbank.ditsib.transport.reports.dto.analytic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(title = "Тип графика", description = "Доступные типы графиков")
public enum ChartType {
    
    TOTAL("total"),
    SLA("SLA"),
    CSI("CSI"),
    BUDGET("budget");
    
    private final String typeName;
    
    public String getName() {
        return typeName;
    }
}
