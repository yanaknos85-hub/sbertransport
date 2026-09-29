package ru.sberbank.ditsib.transport.reports.dto.analytic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(title = "Тип метрики", description = "Доступные типы метрик")
public enum MetricType {
    TOTAL_TOTAL("total","Всего заявок", "unit"),
    TOTAL_EXECUTED("executed", "Выполнено", "unit"),
    TOTAL_UNEXECUTED("unExecuted", "Не выполнено", "unit"),
    TOTAL_CANCELLED("cancelled", "Отменено", "unit"),
    TOTAL_SUM("sum", "Общая сумма", "rub"),
    
    CSI_STAR5("star5","5 звезд", "unit"),
    CSI_STAR4("star4","4 звезды", "unit"),
    CSI_STAR3("star3","3 звезды", "unit"),
    CSI_STAR2("star2","2 звезды", "unit"),
    CSI_STAR1("star1","1 звезда", "unit"),
    CSI_CSI("customerSatisfaction","Удовлетворенные клиенты", "percent"),
    CSI_TOTALEVALUATED("totalEvaluated", "Оцененные заявки", "unit"),
    CSI_STARPOSITIVE("starPositive", "Положительные", "unit"),
    CSI_STARNEGATIVE("starNegative","Отрицательные","unit"),
    CSI_NORM("norm", "Норма", "percent"),
    CSI_FACT("fact","Факт", "percent"),
    
    BUDGET_TOTALBUDGET("totalBudget", "Общий лимит", "rub"),
    BUDGET_SPENT("spent", "Потрачено", "rub"),
    
    SLA_FACT("fact", "% исполнения обязательств", "percent"),
    SLA_NORM("norm", "Норма", "percent"),
    SLA_WITHVIOLATION("withViolation", "Выполненные с нарушениями", "unit"),
    SLA_WITHOUTVIOLATION("withoutViolation","Выполненные без нарушения", "unit");
    
    private final String code;
    private final String typeName;
    private final String typeValue;
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return typeName;
    }
    
    public String getTypeValue() {
        return typeValue;
    }
}
