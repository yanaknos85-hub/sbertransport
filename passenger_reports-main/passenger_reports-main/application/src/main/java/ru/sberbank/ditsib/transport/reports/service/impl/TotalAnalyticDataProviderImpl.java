package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.reports.dao.StatsRepository;
import ru.sberbank.ditsib.transport.reports.dto.analytic.*;
import ru.sberbank.ditsib.transport.reports.model.Stats;
import ru.sberbank.ditsib.transport.reports.service.AnalyticDataProvider;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TotalAnalyticDataProviderImpl implements AnalyticDataProvider {
    
    private final StatsRepository statsRepository;
    
    @Override
    public ChartDTO getChartData(GeneralAnalyticalReportRequestDTO request, List<Stats> statsList) {
        
        log.info("Start: TotalAnalyticDataProviderImpl.getChartData(organizationId={}, year={}, transportTypes={})", request.getOrganizationId(),
                 request.getYear(), request.getTransportTypes());
        
        ChartDTO chart = new ChartDTO();
        chart.setType(ChartType.TOTAL.getName());
        
        List<MetricsPerPeriodDTO> dataPerPeriod = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            dataPerPeriod.add(getMetricsPerPeriod(request, month, statsList));
        }
        chart.setDataPerPeriod(dataPerPeriod);
    
        MetricDTO metricTotalDTO = createMetric(MetricType.TOTAL_TOTAL, dataPerPeriod);
        MetricDTO metricExecutedDTO = createMetric(MetricType.TOTAL_EXECUTED, dataPerPeriod);
        MetricDTO metricUnexecutedDTO = createMetric(MetricType.TOTAL_UNEXECUTED, dataPerPeriod);
        MetricDTO metricCancelledDTO = createMetric(MetricType.TOTAL_CANCELLED, dataPerPeriod);
        MetricDTO metricSumDTO = createMetric(MetricType.TOTAL_SUM, dataPerPeriod);
        
        List<MetricDTO> totals = new ArrayList<>();
        totals.add(metricTotalDTO);
        totals.add(metricExecutedDTO);
        totals.add(metricUnexecutedDTO);
        totals.add(metricCancelledDTO);
        totals.add(metricSumDTO);
        chart.setTotals(totals);
        
        List<MetricDTO> data = new ArrayList<>();
        data.add(metricExecutedDTO);
        data.add(metricUnexecutedDTO);
        data.add(metricCancelledDTO);
        chart.setData(data);
        
        log.info("End: TotalAnalyticDataProviderImpl.getChartData(organizationId={}, year={}, transportTypes={})",
                 request.getOrganizationId(), request.getYear(), request.getTransportTypes());
        
        return chart;
    }
    
    private MetricDTO createMetric(MetricType metricType, List<MetricsPerPeriodDTO> dataPerPeriod) {
        MetricDTO metric = new MetricDTO();
        metric.setCode(metricType.getCode());
        metric.setName(metricType.getName());
        metric.setValue(getValue(dataPerPeriod, metricType));
        metric.setTypeValue(metricType.getTypeValue());
        
        return metric;
    }
    
    private Long getValue(List<MetricsPerPeriodDTO> dataPerPeriod, MetricType metricType) {
        return dataPerPeriod.stream().map(MetricsPerPeriodDTO::getMetrics).flatMap(Collection::stream)
                            .filter(metric -> metric.getCode().equals(metricType.getCode())).mapToLong(MetricDTO::getValue).sum();
    }
    
    private MetricsPerPeriodDTO getMetricsPerPeriod(GeneralAnalyticalReportRequestDTO request, int month, List<Stats> statsList) {
        var year = request.getYear();
        List<Stats> statsValues = new ArrayList<>();
        for (Stats stats : statsList) {
            if (month == stats.getMonth()) {
                statsValues.add(stats);
            }
        }
        
        long valueTotal = statsValues.stream().mapToLong(e -> (e.getTotalExecuted() + e.getTotalNotExecuted() + e.getTotalCanceled())).sum();
        long valueExecuted = statsValues.stream().mapToLong(Stats::getTotalExecuted).sum();
        long valueUnExecuted = statsValues.stream().mapToLong(Stats::getTotalNotExecuted).sum();
        long valueCancelled = statsValues.stream().mapToLong(Stats::getTotalCanceled).sum();
        long valueSum = statsValues.stream().mapToLong(Stats::getTotalSum).sum();
    
        var mpp = new MetricsPerPeriodDTO();
        mpp.setDateFrom(LocalDate.of(year, month, 1));
        mpp.setDateTo(mpp.getDateFrom().withDayOfMonth(mpp.getDateFrom().lengthOfMonth()));
        
        List<MetricDTO> metrics = new ArrayList<>();
        metrics.add(createMetric(MetricType.TOTAL_TOTAL, valueTotal));
        metrics.add(createMetric(MetricType.TOTAL_EXECUTED, valueExecuted));
        metrics.add(createMetric(MetricType.TOTAL_UNEXECUTED, valueUnExecuted));
        metrics.add(createMetric(MetricType.TOTAL_CANCELLED, valueCancelled));
        metrics.add(createMetric(MetricType.TOTAL_SUM, valueSum));
        mpp.setMetrics(metrics);
        
        return mpp;
    }
    
    private MetricDTO createMetric(MetricType metricType, Long value) {
        MetricDTO metric = new MetricDTO();
        metric.setCode(metricType.getCode());
        metric.setName(metricType.getName());
        metric.setValue(value);
        metric.setTypeValue(metricType.getTypeValue());
        
        return metric;
    }
}