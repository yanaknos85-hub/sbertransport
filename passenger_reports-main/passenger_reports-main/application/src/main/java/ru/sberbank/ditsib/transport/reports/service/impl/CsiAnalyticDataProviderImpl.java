package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.dao.StatsRepository;
import ru.sberbank.ditsib.transport.reports.dto.analytic.*;
import ru.sberbank.ditsib.transport.reports.model.Stats;
import ru.sberbank.ditsib.transport.reports.service.AnalyticDataProvider;

import java.time.LocalDate;
import java.util.*;

@SuppressWarnings({ "ClassCanBeRecord", "DuplicatedCode" })
@Slf4j
@RequiredArgsConstructor
public class CsiAnalyticDataProviderImpl implements AnalyticDataProvider {
    
    private final StatsRepository statsRepository;
    
    @Override
    public ChartDTO getChartData(GeneralAnalyticalReportRequestDTO request, List<Stats> statsList) {
        
        log.info("Start: CsiAnalyticDataProviderImpl.getChartData(organizationId={}, year={}, transportTypes={})", request.getOrganizationId(),
                 request.getYear(), request.getTransportTypes());
        
        ChartDTO chart = new ChartDTO();
        chart.setType(ChartType.CSI.getName());
        
        List<MetricsPerPeriodDTO> dataPerPeriodList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            dataPerPeriodList.add(getMetricsPerPeriod(request, month, statsList));
        }
        chart.setDataPerPeriod(dataPerPeriodList);
        
        List<MetricDTO> metrics = new ArrayList<>();
        long starPositive = getStarPositive(dataPerPeriodList);
        long starNegative = getStarNegative(dataPerPeriodList);
        long totalEvaluated = starPositive + starNegative;
        long valueCSI = calcCSI(starPositive, starNegative);
        metrics.add(createMetric(MetricType.CSI_TOTALEVALUATED, totalEvaluated));
        metrics.add(createMetric(MetricType.CSI_STARPOSITIVE, starPositive));
        metrics.add(createMetric(MetricType.CSI_STARNEGATIVE, starNegative));
        metrics.add(createMetric(MetricType.CSI_CSI, valueCSI));
        chart.setTotals(metrics);
        
        List<MetricDTO> data = new ArrayList<>();
        data.add(createMetric(MetricType.CSI_FACT, calcCSI(starPositive, starNegative)));
        data.add(createMetric(MetricType.CSI_NORM, 95L));
        chart.setData(data);
        
        log.info("End: CsiAnalyticDataProviderImpl.getChartData(organizationId={}, year={}, transportTypes={})",
                 request.getOrganizationId(), request.getYear(), request.getTransportTypes());
        
        return chart;
    }
    
    private long getStarPositive(List<MetricsPerPeriodDTO> dataPerPeriod) {
        return getValue(dataPerPeriod, MetricType.CSI_STARPOSITIVE);
    }
    
    private long getStarNegative(List<MetricsPerPeriodDTO> dataPerPeriod) {
        return getValue(dataPerPeriod, MetricType.CSI_STARNEGATIVE);
    }
    
    private long calcCSI(long starPositive, long starNegative) {
        long valueCSI = 0L;
        long totalEvaluated = starPositive + starNegative;
        if (starPositive > 0 && totalEvaluated > 0) {
            valueCSI = starPositive * 100 / totalEvaluated;
        }
        return valueCSI;
    }
    
    private Long getValue(List<MetricsPerPeriodDTO> dataPerPeriod, MetricType metricType) {
        return dataPerPeriod.stream().map(MetricsPerPeriodDTO::getMetrics).flatMap(Collection::stream)
                            .filter(metric -> metric.getCode().equals(metricType.getCode())).mapToLong(MetricDTO::getValue).sum();
    }
    
    MetricsPerPeriodDTO getMetricsPerPeriod(GeneralAnalyticalReportRequestDTO request, int month, List<Stats> statsList) {
        var year = request.getYear();
        List<Stats> statsValues = new ArrayList<>();
        for (Stats stats : statsList) {
            if (month == stats.getMonth()) {
                statsValues.add(stats);
            }
        }
    
        var mpp = new MetricsPerPeriodDTO();
        mpp.setDateFrom(LocalDate.of(year, month, 1));
        mpp.setDateTo(mpp.getDateFrom().withDayOfMonth(mpp.getDateFrom().lengthOfMonth()));
        
        long csiStarPositive = statsValues.stream().mapToLong(e -> (e.getCsiStarPositive())).sum();
        long csiStarNegative = statsValues.stream().mapToLong(e -> (e.getCsiStarNegative())).sum();
        
        long valueCSI = 0L;
        if ((csiStarPositive) > 0 && (csiStarNegative) > 0) {
            valueCSI = (((csiStarPositive) * 1000L / (csiStarPositive + csiStarNegative)) + 5L) / 10L;
        }
        List<MetricDTO> metrics = new ArrayList<>();
        metrics.add(createMetric(MetricType.CSI_STARPOSITIVE, csiStarPositive));
        metrics.add(createMetric(MetricType.CSI_STARNEGATIVE, csiStarNegative));
        metrics.add(createMetric(MetricType.CSI_CSI, valueCSI));
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
