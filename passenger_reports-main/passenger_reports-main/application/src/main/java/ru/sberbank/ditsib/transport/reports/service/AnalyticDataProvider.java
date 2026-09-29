package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.dto.analytic.ChartDTO;
import ru.sberbank.ditsib.transport.reports.dto.analytic.GeneralAnalyticalReportRequestDTO;
import ru.sberbank.ditsib.transport.reports.model.Stats;

import java.util.List;

public interface AnalyticDataProvider {
    ChartDTO getChartData(GeneralAnalyticalReportRequestDTO request, List<Stats> statsList);
}
